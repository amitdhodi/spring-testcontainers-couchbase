package com.example.couchbase.containers.repository;

import com.example.couchbase.containers.domain.Resource;
import org.springframework.data.repository.CrudRepository;

public interface ResourceRepository extends CrudRepository<Resource, String> {
}
