package com.i5018.shiro.session.repository;

import com.i5018.shiro.util.ObjectRedisSerializer;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author limk
 * @date 2020/9/2 10:27
 */
public class SessionRepositoryImpl implements SessionRepository {

    private static final Logger logger = LoggerFactory.getLogger(SessionRepositoryImpl.class);

    private static final String DEFAULT_REDIS_SHIRO_SESSION = "shiro:session:";

    /**
     * redis连接工厂
     */
    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * 缓存前缀
     */
    private String keyPrefix = DEFAULT_REDIS_SHIRO_SESSION;

    /**
     * 缓存有效期（秒）
     */
    private Duration expiration = Duration.ofSeconds(1800);

    /**
     * key序列化工具
     */
    private RedisSerializer<String> serializerKey = new StringRedisSerializer();

    /**
     * value序列化工具
     */
    private RedisSerializer<Object> serializerValue = new ObjectRedisSerializer();

    public SessionRepositoryImpl(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public void saveSession(Session session) {
        if (session == null || session.getId() == null) {
            throw new NullPointerException("session is empty");
        }

        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            String sessionKey = buildRedisSessionKey(session.getId());
            redisConnection.setEx(serializeKey(sessionKey), expiration.getSeconds(), serializeValue(session));
        } catch (Exception e) {
            logger.error("save session error. ", e);
        }
    }

    @Override
    public void updateSession(Session session) {
        if (session == null || session.getId() == null) {
            throw new NullPointerException("session is empty");
        }

        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            String sessionKey = buildRedisSessionKey(session.getId());
            redisConnection.setEx(serializeKey(sessionKey), expiration.getSeconds(), serializeValue(session));
        } catch (Exception e) {
            logger.error("update session error. ", e);
        }
    }

    @Override
    public void refreshSession(Serializable sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("session is empty");
        }

        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            String sessionKey = buildRedisSessionKey(sessionId);
            redisConnection.expire(serializeKey(sessionKey), expiration.getSeconds());
        } catch (Exception e) {
            logger.error("refresh session error. ", e);
        }
    }

    @Override
    public void deleteSession(Serializable sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("session id is empty");
        }

        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            String sessionKey = buildRedisSessionKey(sessionId);
            redisConnection.del(serializeKey(sessionKey));
        } catch (Exception e) {
            logger.error("delete session error. ", e);
        }
    }

    @Override
    public Session getSession(Serializable sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("session id is empty");
        }

        Session session = null;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            String sessionKey = buildRedisSessionKey(sessionId);
            session = (Session) deserializeValue(redisConnection.get(serializeKey(sessionKey)));
        } catch (Exception e) {
            logger.error("get session error. ", e);
        }

        return session;
    }

    @Override
    public Collection<Session> getAllSessions() {
        Collection<Session> sessions = null;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            Set<byte[]> allKeys = allKeys(redisConnection);
            if (!CollectionUtils.isEmpty(allKeys)) {
                List<byte[]> allValues = redisConnection.mGet(allKeys.toArray(new byte[allKeys.size()][]));
                if (!CollectionUtils.isEmpty(allValues)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Currently scanning to {} key-values.", allValues.size());
                    }
                    List<Session> result = allValues.stream().map(value -> (Session) deserializeValue(value))
                            .collect(Collectors.toList());
                    sessions = Collections.unmodifiableList(result);
                }
            }
        } catch (Exception e) {
            logger.error("get all sessions error.", e);
        }

        return sessions;
    }

    @Override
    public Long getActiveSessionsSize() {
        long size = 0L;
        try (RedisConnection redisConnection = redisConnectionFactory.getConnection()) {
            Set<byte[]> allKeys = allKeys(redisConnection);
            if (!CollectionUtils.isEmpty(allKeys)) {
                size = allKeys.size();
            }
        } catch (Exception e) {
            logger.error("get all sessions error.", e);
        }

        return size;
    }

    private String buildRedisSessionKey(Serializable sessionId) {
        return keyPrefix + sessionId;
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

    private byte[] serializeKey(String key) {
        return serializerKey.serialize(key);
    }

    private Serializable deserializeKey(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return serializerKey.deserialize(bytes);
    }

    private byte[] serializeValue(Session value) {
        return serializerValue.serialize(value);
    }

    private Object deserializeValue(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return serializerValue.deserialize(bytes);
    }

    private Set<byte[]> allKeys(RedisConnection redisConnection) {
        Set<byte[]> allKeys = Collections.emptySet();
        byte[] serializeKeys = serializerKey.serialize(keyPrefix + "*");
        if (serializeKeys != null) {
            allKeys = redisConnection.keys(serializeKeys);
        }

        return allKeys;
    }

}
