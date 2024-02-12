package com.cache.springdistrocache.example;

import com.cache.springdistrocache.models.DocumentDto;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Product implements DocumentDto<Integer> {

    private Integer id;

    private String name;

    private String description;

    @Override
    public Integer getCacheKey() {
        return this.id;
    }
}
