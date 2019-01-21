package com.example.couchbase.containers.infrastructure;

import com.couchbase.client.core.utils.Base64;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.Index;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.HttpWaitStrategy;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class CouchbaseContainer<Self extends CouchbaseContainer<Self>> extends GenericContainer<Self> {

    private String memoryQuota = "400";

    private String indexMemoryQuota = "400";

    private String clusterUsername = "Administrator";

    private String clusterPassword = "password";

    private Boolean keyValue = true;

    private Boolean query = true;

    private Boolean index = false;

    private Boolean fts = true;

    private Boolean beerSample = false;

    private Boolean travelSample = false;

    private Boolean gamesIMSample = false;

    private CouchbaseEnvironment couchbaseEnvironment;

    private CouchbaseCluster couchbaseCluster;

    private List<BucketSettings> newBuckets = new ArrayList<>();

    private String urlBase;

    public CouchbaseContainer() {
        super("couchbase/server:community-5.0.1");
    }

    public CouchbaseContainer(String containerName) {
        super(containerName);
    }

    @Override
    protected Integer getLivenessCheckPort() {
        return getMappedPort(8091);
    }

    @Override
    protected void configure() {
        addFixedExposedPort(8091, 8091);
        addFixedExposedPort(8092, 8092);
        addFixedExposedPort(8093, 8093);
        addFixedExposedPort(8094, 8094);
        addFixedExposedPort(8095, 8095);
        addFixedExposedPort(1024, 1024);
        addFixedExposedPort(11207, 11207);
        addFixedExposedPort(11210, 11210);
        addFixedExposedPort(11211, 11211);
        addFixedExposedPort(18091, 18091);
        addFixedExposedPort(18092, 18092);
        addFixedExposedPort(18093, 18093);
        setWaitStrategy(new HttpWaitStrategy().forPath("/ui/index.html#/"));
    }

    public CouchbaseEnvironment getCouchbaseEnvironnement() {
        if (couchbaseEnvironment == null) {
            initCluster();
            couchbaseEnvironment = DefaultCouchbaseEnvironment.builder()
                    .bootstrapCarrierDirectPort(getMappedPort(11210))
                    .bootstrapCarrierSslPort(getMappedPort(11207))
                    .bootstrapHttpDirectPort(getMappedPort(8091))
                    .bootstrapHttpSslPort(getMappedPort(18091))
                    .build();
        }
        return couchbaseEnvironment;
    }

    public CouchbaseCluster getCouchbaseCluster() {
        if (couchbaseCluster == null) {
            couchbaseCluster = CouchbaseCluster.create(getCouchbaseEnvironnement(), getContainerIpAddress());
        }
        return couchbaseCluster;
    }

    public Self withClusterUsername(String username) {
        this.clusterUsername = username;
        return self();
    }

    public Self withClusterPassword(String password) {
        this.clusterPassword = password;
        return self();
    }

    public Self withMemoryQuota(String memoryQuota) {
        this.memoryQuota = memoryQuota;
        return self();
    }

    public Self withIndexMemoryQuota(String indexMemoryQuota) {
        this.indexMemoryQuota = indexMemoryQuota;
        return self();
    }

    public Self withKeyValue(Boolean withKV) {
        this.keyValue = withKV;
        return self();
    }

    public Self withIndex(Boolean withIndex) {
        this.index = withIndex;
        return self();
    }

    public Self withQuery(Boolean withQuery) {
        this.query = withQuery;
        return self();
    }

    public Self withFTS(Boolean withFTS) {
        this.fts = withFTS;
        return self();
    }

    public Self withTravelSample(Boolean withTravelSample) {
        this.travelSample = withTravelSample;
        return self();
    }

    public Self withBeerSample(Boolean withBeerSample) {
        this.beerSample = withBeerSample;
        return self();
    }

    public Self withGamesIMSample(Boolean withGamesIMSample) {
        this.gamesIMSample = withGamesIMSample;
        return self();
    }

    public Self withNewBucket(BucketSettings bucketSettings) {
        newBuckets.add(bucketSettings);
        return self();
    }


    public void initCluster() {
        urlBase = String.format("http://%s:%s", getContainerIpAddress(), getMappedPort(8091));
        try {
            String poolURL = "/pools/default";
            String poolPayload = "memoryQuota=" + URLEncoder.encode(memoryQuota, "UTF-8") + "&indexMemoryQuota="
                    + URLEncoder.encode(indexMemoryQuota, "UTF-8");

            String setupServicesURL = "/node/controller/setupServices";
            StringBuilder servicePayloadBuilder = new StringBuilder();
            if (keyValue) {
                servicePayloadBuilder.append("kv,");
            }
            if (query) {
                servicePayloadBuilder.append("n1ql,");
            }
            if (index) {
                servicePayloadBuilder.append("index,");
            }
            if (fts) {
                servicePayloadBuilder.append("fts,");
            }
            String setupServiceContent = "services=" + URLEncoder.encode(servicePayloadBuilder.toString(), "UTF-8");

            String webSettingsURL = "/settings/web";
            String webSettingsContent = "username=" + URLEncoder.encode(clusterUsername, "UTF-8") + "&password="
                    + URLEncoder.encode(clusterPassword, "UTF-8") + "&port=8091";

            String bucketURL = "/sampleBuckets/install";

            StringBuilder sampleBucketPayloadBuilder = new StringBuilder();
            sampleBucketPayloadBuilder.append('[');
            if (travelSample) {
                sampleBucketPayloadBuilder.append("\"travel-sample\",");
            }
            if (beerSample) {
                sampleBucketPayloadBuilder.append("\"beer-sample\",");
            }
            if (gamesIMSample) {
                sampleBucketPayloadBuilder.append("\"gamesim-sample\",");
            }
            sampleBucketPayloadBuilder.append(']');

            callCouchbaseRestAPI(poolURL, poolPayload);
            callCouchbaseRestAPI(setupServicesURL, setupServiceContent);
            callCouchbaseRestAPI(webSettingsURL, webSettingsContent);
            callCouchbaseRestAPI(bucketURL, sampleBucketPayloadBuilder.toString());

            CouchbaseWaitStrategy s = new CouchbaseWaitStrategy();

            s.withBasicCredentials(clusterUsername, clusterPassword);
            s.waitUntilReady(this);

            callCouchbaseRestAPI("/settings/indexes",
                    "indexerThreads=0&logLevel=info&maxRollbackPoints=5&storageMode=memory_optimized");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createBucket(BucketSettings bucketSetting, Boolean createIndex) {
        getCouchbaseCluster().clusterManager(clusterUsername, clusterPassword).insertBucket(bucketSetting);
        // allow some time for the query service to come up
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (createIndex) {
            getCouchbaseCluster()
                    .openBucket(clusterUsername, clusterPassword)
                    .query(Index.createPrimaryIndex()
                            .on(bucketSetting.name()));
        }
    }

    public void callCouchbaseRestAPI(String url, String payload) throws IOException {
        String fullUrl = urlBase + url;
        HttpURLConnection httpConnection = (HttpURLConnection) ((new URL(fullUrl).openConnection()));
        httpConnection.setDoOutput(true);
        httpConnection.setRequestMethod("POST");
        httpConnection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        String encoded = Base64.encode((clusterUsername + ":" + clusterPassword).getBytes("UTF-8"));
        httpConnection.setRequestProperty("Authorization", "Basic " + encoded);
        DataOutputStream out = new DataOutputStream(httpConnection.getOutputStream());
        out.writeBytes(payload);
        out.flush();
        out.close();
        httpConnection.getResponseCode();
        httpConnection.disconnect();
    }

    @Override
    public void start() {
        super.start();
        if (!newBuckets.isEmpty()) {
            for (BucketSettings bucketSetting : newBuckets) {
                createBucket(bucketSetting, index);
            }
        }
    }

}
