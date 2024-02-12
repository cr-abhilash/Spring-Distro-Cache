package com.cache.springdistrocache.example.reactiveMongoExample;

import com.cache.springdistrocache.example.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, Integer> {

    Mono<Product> findByName(@Param("name") String name);
}
