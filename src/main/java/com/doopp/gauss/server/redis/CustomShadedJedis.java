package com.doopp.gauss.server.redis;

import com.doopp.gauss.server.util.SerializeUtils;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CustomShadedJedis {

    private final Logger logger = LoggerFactory.getLogger(CustomShadedJedis.class);

    private ShardedJedisPool shardedJedisPool;

    public void setex(String key, int seconds, String value) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.setex(key, seconds, value);
        }
    }

    public void set(String key, String value) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.set(key, value);
        }
    }

    public <T> Map<String, T> hgetall(String key, Class<T> clazz) {
        Map<String, T> result = Maps.newHashMap();
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            Map<String, String> ret = shardedJedis.hgetAll(key);

            for (String k : ret.keySet()) {
                Object t = new GsonBuilder().serializeNulls().create().fromJson(ret.get(k), Object.class);
                result.put(k, clazz.cast(t));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public <T> T hget(String key, String subkey, Class<T> clazz) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            String ret = shardedJedis.hget(key, subkey);
            if (ret != null) {
                Object t = new GsonBuilder().serializeNulls().create().fromJson(ret, Object.class);
                return clazz.cast(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String get(String key) {
        String value;
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            value = shardedJedis.get(key);
        }
        return value;
    }

    public void del(String... keys) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            for (String key : keys) {
                shardedJedis.del(key);
            }
        }
    }

    public void setex(byte[] key, int seconds, Object object) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            byte[] _object = SerializeUtils.serialize(object);
            shardedJedis.setex(key, seconds, _object);
        }
    }

    public <T> void set(byte[] key, T object) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            byte[] _object = SerializeUtils.serialize(object);
            shardedJedis.set(key, _object);
        }
    }

    public <T> T get(byte[] key, Class<T> clazz) {
        byte[] _object;
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            _object = shardedJedis.get(key);
        }
        if (_object == null) {
            return null;
        }
        return clazz.cast(SerializeUtils.deSerialize(_object));
    }

    public void del(byte[]... keys) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            for (byte[] key : keys) {
                shardedJedis.del(key);
            }
        }
    }

    public <T> void setList(String key, List<T> list) {
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            shardedJedis.set(key.getBytes(), SerializeUtils.serialize(list));
        }
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        byte[] in;
        try (ShardedJedis shardedJedis = shardedJedisPool.getResource()) {
            in = shardedJedis.get(key.getBytes());
        }
        // return (List<T>) redisSerializer.deserialize(in);
        List listDes = (List) SerializeUtils.deSerialize(in);
        List<T> listRst = new ArrayList<>();
        for (Object listItem : listDes) {
            listRst.add(clazz.cast(listItem));
        }
        return listRst;
    }

    public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
        // this.shardedJedis = shardedJedisPool.getResource();
    }

    public ShardedJedisPool getShardedJedisPool() {
        return this.shardedJedisPool;
    }
}
