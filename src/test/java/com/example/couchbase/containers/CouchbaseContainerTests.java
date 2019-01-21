package com.example.couchbase.containers;

import com.example.couchbase.containers.domain.Resource;
import com.example.couchbase.containers.repository.ResourceRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class CouchbaseContainerTests extends BaseIntegrationTest {

	@Autowired
	private ResourceRepository resourceRepository;

	private Resource resource;

	@Before
	public void before(){
		resource = new Resource("1", "ContainerTest", "35");
		resourceRepository.save(resource);
	}

	@Test
	public void couchbaseContainerTest() {
		Resource dbResource = resourceRepository.findOne("1");
		if (dbResource != null){
			assertEquals("1", dbResource.getId());
			assertEquals("ContainerTest", dbResource.getName());
			assertEquals("35", dbResource.getAge());
		} else {
			throw new RuntimeException("Resource not found!!");
		}
	}

	@After
	public void after(){
		resourceRepository.delete(resource);
	}
}

