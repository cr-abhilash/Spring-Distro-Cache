package com.cache.springdistrocache.cache;

import com.cache.springdistrocache.models.CacheType;
import reactor.core.publisher.Mono;

public interface BaseCache {
    /**
     * To refresh the cache
     * @return Success Message
     */
    Mono<String> refreshCache();

    /**
     * To get hds cache type
     * @return type of hds cache enum
     */
    CacheType getCacheType();

}
