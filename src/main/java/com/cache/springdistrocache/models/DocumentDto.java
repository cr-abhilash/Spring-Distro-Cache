package com.cache.springdistrocache.models;

public interface DocumentDto<P> {
    /**
     * Method to get cache key used in cache
     * @return cacheKey (primaryKey of document).
     */
    P getCacheKey();
}
