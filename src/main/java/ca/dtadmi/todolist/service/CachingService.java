package ca.dtadmi.todolist.service;

import ca.dtadmi.todolist.dto.TaskResultDto;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CachingService {
    CacheManager cacheManager;

    public CachingService(CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    public Object getSingleCacheValue(String cacheName, String cacheKey) {
        Cache cache = cacheManager.getCache(cacheName);
        if(cache!=null){
            return cache.get(cacheKey, TaskResultDto.class);
        }

        return null;
    }

    public void cacheSingleValue(String cacheName, String cacheKey, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if(cache!=null){
            cache.put(cacheKey, value);
        }
    }

    public void evictSingleCacheValue(String cacheName, String cacheKey) {
        Cache cache = cacheManager.getCache(cacheName);
        if(cache!=null){
            cache.evict(cacheKey);
        }
    }

    public void evictAllCacheValues(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if(cache!=null){
            cache.clear();
        }
    }
    public void evictAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    Cache cache = cacheManager.getCache(cacheName);
                    if(cache!=null){
                        cache.clear();
                    }
                });
    }
    @Scheduled(fixedRate = 6000)
    public void evictAllcachesAtIntervals() {
        evictAllCaches();
    }
}
