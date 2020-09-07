package com.i5018.shiro.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.*;

/**
 * @author limk
 * @date 2020/9/1 10:43
 */
public class ObjectRedisSerializer implements RedisSerializer<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectRedisSerializer.class);

    @Override
    public byte[] serialize(Object o) throws SerializationException {
        byte[] result = null;
        if (o == null) {
            return new byte[0];
        }
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
            if (!(o instanceof Serializable)) {
                throw new IllegalArgumentException(ObjectRedisSerializer.class.getSimpleName() + " requires a Serializable payload " +
                        "but received an object of type [" + o.getClass().getName() + "]");
            }
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
            result = byteStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Failed to serialize", e);
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        Object result = null;

        if (isEmpty(bytes)) {
            return null;
        }

        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
            result = objectInputStream.readObject();
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize", e);
            e.printStackTrace();
        }
        return result;
    }

    private static boolean isEmpty(byte[] data) {
        return (data == null || data.length == 0);
    }

}
