package com.i5018.shiro.cache;

import com.i5018.shiro.cache.ExpiredCache.Named;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author limk
 * @date 2020/9/1 10:04
 */
public class RedisCache<K, V> implements ExpiredCache<K, V>, Named {

    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * redis连接工厂
     */
    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * 缓存前缀
     */
    private String keyPrefix;

    /**
     * 缓存有效期
     */
    private Duration expiration;

    /**
     * key序列化工具
     */
    private RedisSerializer<String> serializerKey;

    /**
     * value序列化工具
     */
    private RedisSerializer<Object> serializerValue;

    public RedisCache(String name, RedisConnectionFactory redisConnectionFactory) {
        this.name = name;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public RedisSerializer<String> getSerializerKey() {
        return serializerKey;
    }

    public void setSerializerKey(RedisSerializer<String> serializerKey) {
        this.serializerKey = serializerKey;
    }

    public RedisSerializer<Object> getSerializerValue() {
        return serializerValue;
    }

    public void setSerializerValue(RedisSerializer<Object> serializerValue) {
        this.serializerValue = serializerValue;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) throws CacheException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting object from cache [" + getName() + "] for key [" + key + "]");
        }

        if (key == null) {
            return null;
        }

        V result = null;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            result = (V) deserializeValue(redisConnection.get(serializeKey(key)));
        } catch (Exception e) {
            logger.error("shiro redis cache get exception. ", e);
        }

        return result;
    }

    @Override
    public V put(K key, V value) throws CacheException {
        return put(key, value, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value, Duration expired) throws CacheException {
        if (logger.isDebugEnabled()) {
            logger.debug("Putting object in cache [" + getName() + "] for key [" + key + "]");
        }

        if (key == null) {
            return value;
        }

        V result = null;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            result = (V) deserializeValue(redisConnection.get(serializeKey(key)));

            Duration expiration = expired != null ? expired : getExpiration();
            redisConnection.setEx(serializeKey(key), expiration.getSeconds(), serializeValue(value));
        } catch (Exception e) {
            logger.error("shiro redis cache put exception. ", e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(K key) throws CacheException {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing object from cache [" + getName() + "] for key [" + key + "]");
        }
        if (key == null) {
            return null;
        }

        V result = null;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            result = (V) deserializeValue(redisConnection.get(serializeKey(key)));
            redisConnection.del(serializeKey(key));
        } catch (Exception e) {
            logger.error("shiro redis cache remove exception. ", e);
        }

        return result;
    }

    @Override
    public void clear() throws CacheException {
        if (logger.isDebugEnabled()) {
            logger.debug("Clear all cached objects.");
        }

        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            Set<byte[]> allKeys = allKeys(redisConnection, name);
            if (!CollectionUtils.isEmpty(allKeys)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Currently scanning to {} keys, ready to clear.", allKeys.size());
                }

                redisConnection.del(allKeys.toArray(new byte[allKeys.size()][]));
            }
        } catch (Exception e) {
            logger.error("shiro redis cache clear exception.", e);
        }
    }

    @Override
    public int size() {
        if (logger.isDebugEnabled()) {
            logger.debug("shiro redis cache size.{}", name);
        }

        int length = 0;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            Set<byte[]> allKeys = allKeys(redisConnection, name);
            if (!CollectionUtils.isEmpty(allKeys)) {
                length = allKeys.size();
            }
        } catch (Exception e) {
            logger.error("shiro redis cache size exception.", e);
        }

        return length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keys() {
        if (logger.isDebugEnabled()) {
            logger.debug("shiro redis cache keys.{}", name);
        }

        Set<K> resultSet = null;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            Set<byte[]> allKeys = allKeys(redisConnection, name);
            if (!CollectionUtils.isEmpty(allKeys)) {
                resultSet = allKeys.stream()
                        .map(key -> (K) deserializeKey(key))
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            logger.error("shiro redis cache keys exception.", e);
        }

        return resultSet;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        if (logger.isDebugEnabled()) {
            logger.debug("shiro redis cache values.{}", name);
        }

        Collection<V> values = null;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            Set<byte[]> allKeys = allKeys(redisConnection, name);
            if (!CollectionUtils.isEmpty(allKeys)) {
                List<byte[]> allValues = redisConnection.mGet(allKeys.toArray(new byte[allKeys.size()][]));
                if (!CollectionUtils.isEmpty(allValues)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Currently scanning to {} key-values.", allValues.size());
                    }
                    List<V> result = allValues.stream().map(v -> ((V) deserializeValue(v)))
                            .collect(Collectors.toList());
                    values = Collections.unmodifiableList(result);
                }
            }
        } catch (Exception e) {
            logger.error("shiro redis cache keys exception.", e);
        }

        return values;
    }

    private String generateKey(K key) {
        return keyPrefix + name + ":" + key;
    }

    private byte[] serializeKey(K key) {
        return serializerKey.serialize(generateKey(key));
    }

    private String deserializeKey(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return serializerKey.deserialize(bytes);
    }

    private byte[] serializeValue(Object value) {
        return serializerValue.serialize(value);
    }

    private Object deserializeValue(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return serializerValue.deserialize(bytes);
    }

    private Set<byte[]> allKeys(RedisConnection redisConnection, String name) {
        Set<byte[]> allKeys = Collections.emptySet();

        String key = StringUtils.isEmpty(name) ? keyPrefix + "*" : keyPrefix + name + "*";
        byte[] serializeKeys = serializerKey.serialize(key);
        if (serializeKeys != null) {
            allKeys = redisConnection.keys(serializeKeys);
        }

        return allKeys;
    }

}
