# spring-testcontainers-couchbase
Spring boot maven project which runs couchbase cluster as container to facilitate running of integration tests without the actual couchbase installation

# Steps to test couchbase container
1. Git clone https://github.com/amitdhodi/spring-testcontainers-couchbase.git
2. Import maven project in IDE
3. Run "couchbaseContainerTest" test case present in "CouchbaseContainerTests.java" test class
4. This test would query the persisted "Resource" object in repository and then assert it's values
