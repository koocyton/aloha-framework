package com.doopp.gauss.server.redis;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CustomShadedJedis {

    private final Logger logger = LoggerFactory.getLogger(CustomShadedJedis.class);

    private ShardedJedisPool shardedJedisPool;

    public CustomShadedJedis(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
    }

    public String get(String key) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            return shardedJedis.get(key);
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            byte[] result = shardedJedis.get(key.getBytes());
            if (result==null) {
                return null;
            }
            return this.deserialize(result, clazz);
        }
    }

    public void set(String key, Object obj) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.set(key.getBytes(), this.serialize(obj));
        }
    }

    public void set(String key, String value) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.set(key, value);
        }
    }

    public void setex(String key, int seconds, String value) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.setex(key, seconds, value);
        }
    }

    public void setex(String key, int seconds, Object obj) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.setex(key.getBytes(), seconds, this.serialize(obj));
        }
    }

    public void hset(String key, String field, Object obj) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.hset(key.getBytes(), field.getBytes(), this.serialize(obj));
        }
    }

    public <T> T hget(String key, String field, Class<T> clazz) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            byte[] ret = shardedJedis.hget(key.getBytes(), field.getBytes());
            if (ret==null) {
                return null;
            }
            return this.deserialize(ret, clazz);
        }
    }

    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            Map<byte[], byte[]> ret = shardedJedis.hgetAll(key.getBytes());
            Map<String, T> result = new HashMap<>();
            for (byte[] k : ret.keySet()) {
                result.put(new String(k), this.deserialize(ret.get(k), clazz));
            }
            return result;
        }
    }

    public void del(String... keys) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            for (String key : keys) {
                shardedJedis.del(key);
            }
        }
    }

    public void del(byte[]... keys) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            for (byte[] key : keys) {
                shardedJedis.del(key);
            }
        }
    }

    private byte[] serialize(Object object) {
        Kryo kryo = new Kryo();
        Output output = new Output(new byte[2048]);
        kryo.writeObject(output, object);
        output.close();
        return output.toBytes();
    }

    private <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = new Kryo();
        return kryo.readObject(new Input(bytes), clazz);
    }
}
