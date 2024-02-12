package com.cache.springdistrocache.example.reactiveMongoExample;

import com.cache.springdistrocache.example.CustomCacheType;
import com.cache.springdistrocache.example.Product;
import com.cache.springdistrocache.models.CacheType;
import com.cache.springdistrocache.reactiveMongoCache.ReactiveMongoCache;

public class ProductRepositoryCache extends ReactiveMongoCache<ProductRepository, Product, Integer> {
    @Override
    public CacheType getCacheType() {
        return CustomCacheType.PRODUCT;
    }
}
