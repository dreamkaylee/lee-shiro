package com.i5018.shiro.cache;

import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * @author limk
 * @date 2020/9/3 14:06
 */
public interface ExpiredCacheManager extends CacheManager {

    @Override
    <K, V> ExpiredCache<K, V> getCache(String name) throws CacheException;
}
