package com.i5018.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.time.Duration;

/**
 * @author limk
 * @date 2020/9/1 10:05
 */
public interface ExpiredCache<K, V> extends Cache<K, V> {

    /**
     * Adds a Cache entry.
     *
     * @param key     the key used to identify the object being stored.
     * @param value   the value to be stored in the cache.
     * @param expired the expired used to identify the expiration time of the cache.
     * @return the previous value associated with the given {@code key} or {@code null} if there was previous value
     * @throws CacheException if there is a problem accessing the underlying cache system
     */
    V put(K key, V value, Duration expired) throws CacheException;

    /**
     * Instance with name.
     */
    interface Named {

        /**
         * Get the specified cache name
         *
         * @return specified cache name
         */
        String getName();
    }

}
