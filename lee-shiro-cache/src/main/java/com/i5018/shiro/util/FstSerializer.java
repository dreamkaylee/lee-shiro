package com.i5018.shiro.util;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author limk
 * @date 2020/9/1 17:25
 */
public class FstSerializer implements RedisSerializer<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FstSerializer.class);

    private final FSTConfiguration conf;

    public FstSerializer() {
        super();
        this.conf = FSTConfiguration.createDefaultConfiguration();
    }

    @Override
    public byte[] serialize(Object o) throws SerializationException {
        if (o == null) {
            return new byte[0];
        }

        if (!(o instanceof Serializable)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " requires a Serializable payload "
                    + "but received an object of type [" + o.getClass().getName() + "]");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        FSTObjectOutput objectOutput = conf.getObjectOutput(out);
        try {
            objectOutput.writeObject(o);
            return objectOutput.getCopyOfWrittenBuffer();
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize object of type: " + o.getClass().getName(), e);
        } finally {
            try {
                objectOutput.flush();
                out.close();
            } catch (Exception ex) {
                LOGGER.error("Failed to serialize", ex);
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (isEmpty(bytes)) {
            return null;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        FSTObjectInput objectInput = conf.getObjectInput(inputStream);
        try {
            return objectInput.readObject();
        } catch (Exception e) {
            throw new SerializationException(
                    "Failed to deserialize payload. " + "Is the byte array a result of corresponding serialization for "
                            + this.getClass().getSimpleName() + "?", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.error("Failed to deserialize", e);
                e.printStackTrace();
            }
        }
    }

    private static boolean isEmpty(byte[] data) {
        return (data == null || data.length == 0);
    }

}
