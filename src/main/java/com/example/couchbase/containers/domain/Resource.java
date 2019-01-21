package com.example.couchbase.containers.domain;

import com.couchbase.client.java.repository.annotation.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.couchbase.core.mapping.Document;

@Document
@Getter
@Setter
@Builder
public class Resource {
    @Id
    private String id;
    private String name;
    private String age;

    public Resource(String id, String name, String age){
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getAge(){
        return this.age;
    }
}
