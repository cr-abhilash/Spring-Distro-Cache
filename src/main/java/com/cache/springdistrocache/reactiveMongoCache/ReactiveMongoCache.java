package com.cache.springdistrocache.reactiveMongoCache;

import com.cache.springdistrocache.cache.BaseCache;
import com.cache.springdistrocache.models.DocumentDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public abstract class ReactiveMongoCache<R extends ReactiveMongoRepository<D,P>, D extends DocumentDto<P>, P> implements BaseCache {

    private Map<P, D> cacheData = new HashMap<>();

    private Class<P> pClass;

    private Boolean isCacheDataRefreshing=false;

    @Autowired
    protected R repository;

    public Mono<D> findById(P id){
        return this.isCacheKeyEmpty(id)
                .flatMap(isCacheKeyEmpty ->{
                    if(isCacheKeyEmpty)
                        return repository.findById(id)
                            .doOnSuccess(this::updateCacheDataById);
                    return Mono.just(cacheData.get(id));
                });

    }

    public Flux<D> findAll(){
        return this.isCacheEmpty()
                .flatMapMany(isCacheEmpty -> {
                    if(isCacheEmpty) return this.repository.findAll();
                    return Flux.fromIterable(this.cacheData.values());
                });
    }

    public Mono<D> save(D data){
        return repository.save(data)
                .map(savedData -> {
                        cacheData.put((P)savedData.getCacheKey(),savedData);
                        return savedData;
                 });
    }

    public Flux<D> saveAll(List<D> data){
        return repository.saveAll(data);

    }

    public Mono<Boolean> existById(P id){
        return this.isCacheEmpty()
                .flatMap(isCacheEmpty -> {
                    if(isCacheEmpty) return this.repository.existsById(id);
                    return Mono.just(this.cacheData.containsKey(id));
                });

    }

    public Mono<Void> deleteById(P id){
        return this.repository.deleteById(id)
                .map(data -> this.clearById(id.toString()))
                .then();
    }

    public Mono<Void> deleteAll(){
        return this.repository.deleteAll()
                .then();
    }

    public Mono<Integer> count(){
        return this.isCacheEmpty()
                .flatMap(isCacheEmpty -> {
                    if(isCacheEmpty) this.repository.count();
                    return Mono.just(this.cacheData.size());
                });
    }

    public Mono<Boolean> isCacheEmpty(){
        Boolean value = this.cacheData == null || this.cacheData.isEmpty();
        if(value) this.refreshCache().subscribe();
        return Mono.just(value);

    }

    public Mono<Boolean> isCacheKeyEmpty(P key){
        return this.isCacheEmpty()
                .map(isCacheEmpty -> isCacheEmpty || !this.cacheData.containsKey(key));
    }

    @Override
    public Mono<String> refreshCache(){
        if(this.isCacheDataRefreshing) return Mono.just("Cache refresh action is already triggered for " +this.getCacheType());
        this.setIsCacheDataRefreshing(true);
        return this.repository.findAll()
                .collectMap(data -> (P)data.getCacheKey(),data->data)
                .map(savedMapData -> {
                    this.updateCacheData(savedMapData);
                    this.setIsCacheDataRefreshing(false);
                    return savedMapData;
                })
                .then(Mono.just("Cache refreshed successfully for " +this.getCacheType()))
                .doOnSuccess(d-> log.debug("Cache refreshed successfully for " +this.getCacheType()))
                .onErrorResume(throwable -> {
                    this.setIsCacheDataRefreshing(false);
                    log.error("Failed to refresh cache for "+ this.getCacheType() + ": " + throwable.getMessage());
                    return Mono.just("Failed to refresh cache for"+ this.getCacheType() + ": " + throwable.getMessage());
                });
    }


    public Mono<String> clearCache(){
        this.clearCacheData();
        return Mono.just(this.getCacheType() + " Cache is cleared successfully");
    }

    public Mono<String> clearById(String id){
        this.clearCacheDataById(castPrimaryKey(id));
        return Mono.just("Item removed successfully");
    }

    public Mono<String> refreshById(String id){
        return this.findById(castPrimaryKey(id)).map(d-> "Item removed successfully");
    }

    public P castPrimaryKey(String id){
        try {
             if(pClass.equals(Integer.class)) {
                 Integer castId = Integer.valueOf(id);
                 return (P)castId;
             }
             else return pClass.cast(id);
        }
        catch (Exception e){
            return null;
        }
    }

    public void updateCacheData(Map<P, D> data) {
        this.setCacheData(data);
    }

    public void updateCacheDataById(D data) {
        this.cacheData.put((P)data.getCacheKey(), data);
    }

    public void clearCacheData(){
        this.cacheData.clear();
    }

    public void clearCacheDataById(P id){
        this.cacheData.remove(id);
    }
}
