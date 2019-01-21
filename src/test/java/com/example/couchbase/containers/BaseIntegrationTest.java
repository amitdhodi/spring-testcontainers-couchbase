package com.example.couchbase.containers;

import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.example.couchbase.containers.infrastructure.CouchbaseContainer;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {
        Application.class})
@DirtiesContext
@Ignore
public class BaseIntegrationTest {

    public static final String clusterUser = "containerTest";
    public static final String bucketName = "containerTest";
    public static final String clusterPassword = "containerTest";
    public static CouchbaseContainer couchbaseContainer = new CouchbaseContainer()
            .withFTS(true)
            .withIndex(true)
            .withQuery(true)
            .withClusterUsername(clusterUser)
            .withClusterPassword(clusterPassword)
            .withNewBucket(DefaultBucketSettings.builder()
                    .enableFlush(true)
                    .name(bucketName)
                    .quota(101)
                    .replicas(0)
                    .type(BucketType.COUCHBASE)
                    .port(42008)
                    .build());

    static {
        couchbaseContainer.start();
    }
}