package com.i5018.shiro.cache;

import com.i5018.shiro.util.ObjectRedisSerializer;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import java.time.Duration;

/**
 * @author limk
 * @date 2020/9/1 10:04
 */
public class RedisCacheManager implements ExpiredCacheManager, Destroyable {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheManager.class);

    /**
     * key前缀
     */
    private static final String REDIS_SHIRO_CACHE_KEY_PREFIX = "shiro:cache:";

    /**
     * redis连接工厂
     */
    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * 缓存前缀
     */
    private String keyPrefix = REDIS_SHIRO_CACHE_KEY_PREFIX;

    /**
     * 缓存有效期
     */
    private Duration expiration = Duration.ofMinutes(30);

    /**
     * key序列化工具
     */
    private RedisSerializer<String> serializerKey = new StringRedisSerializer();

    /**
     * value序列化工具
     */
    private RedisSerializer<Object> serializerValue = new ObjectRedisSerializer();

    public RedisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public <K, V> ExpiredCache<K, V> getCache(String name) throws CacheException {
        if (logger.isDebugEnabled()) {
            logger.debug("shiro redis cache manager get cache. name={} ", name);
        }

        RedisCache<K, V> cache = new RedisCache<>(name, redisConnectionFactory);
        cache.setKeyPrefix(keyPrefix);
        cache.setExpiration(expiration);
        cache.setSerializerKey(serializerKey);
        cache.setSerializerValue(serializerValue);
        return cache;
    }

    @Override
    public void destroy() throws DestroyFailedException {

    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public void setSerializerKey(RedisSerializer<String> serializerKey) {
        this.serializerKey = serializerKey;
    }

    public void setSerializerValue(RedisSerializer<Object> serializerValue) {
        this.serializerValue = serializerValue;
    }

}
