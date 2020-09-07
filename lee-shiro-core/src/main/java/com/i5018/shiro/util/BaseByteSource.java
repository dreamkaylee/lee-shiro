package com.i5018.shiro.util;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.util.ByteSource;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author limk
 * @date 2020/8/25 16:21
 */
public class BaseByteSource implements ByteSource, Serializable {

    private byte[] bytes;
    private String cachedHex;
    private String cachedBase64;

    public BaseByteSource() {
    }

    public BaseByteSource(byte[] bytes) {
        this.bytes = bytes;
    }


    public BaseByteSource(char[] chars) {
        this.bytes = CodecSupport.toBytes(chars);
    }


    public BaseByteSource(String string) {
        this.bytes = CodecSupport.toBytes(string);
    }


    public BaseByteSource(ByteSource source) {
        this.bytes = source.getBytes();
    }


    public BaseByteSource(File file) {
        this.bytes = new BytesHelper().getBytes(file);
    }


    public BaseByteSource(InputStream stream) {
        this.bytes = new BytesHelper().getBytes(stream);
    }

    public static boolean isCompatible(Object o) {
        return o instanceof byte[] || o instanceof char[] || o instanceof String ||
                o instanceof ByteSource || o instanceof File || o instanceof InputStream;
    }

    @Override
    public byte[] getBytes() {
        return this.bytes;
    }

    @Override
    public boolean isEmpty() {
        return this.bytes == null || this.bytes.length == 0;
    }

    @Override
    public String toHex() {
        if (this.cachedHex == null) {
            this.cachedHex = Hex.encodeToString(getBytes());
        }
        return this.cachedHex;
    }

    @Override
    public String toBase64() {
        if (this.cachedBase64 == null) {
            this.cachedBase64 = Base64.encodeToString(getBytes());
        }
        return this.cachedBase64;
    }

    @Override
    public String toString() {
        return toBase64();
    }

    @Override
    public int hashCode() {
        if (this.bytes == null || this.bytes.length == 0) {
            return 0;
        }
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ByteSource) {
            ByteSource bs = (ByteSource) o;
            return Arrays.equals(getBytes(), bs.getBytes());
        }
        return false;
    }

    private static final class BytesHelper extends CodecSupport {

        private BytesHelper() {
        }

        byte[] getBytes(File file) {
            return toBytes(file);
        }

        byte[] getBytes(InputStream stream) {
            return toBytes(stream);
        }
    }

}
