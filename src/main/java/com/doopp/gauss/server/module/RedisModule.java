package com.doopp.gauss.server.module;

import com.doopp.gauss.server.redis.CustomShadedJedis;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;

public class RedisModule extends AbstractModule {

    @Override
    public void configure() {
    }

    @Singleton
    @Provides
    @Named("userSessionRedis")
    public CustomShadedJedis userSessionRedis(JedisPoolConfig jedisPoolConfig, @Named("redis.session.userRedisServers") String userRedisServer) {
        ShardedJedisPool shardedJedisPool = this.shardedJedisPool(jedisPoolConfig, userRedisServer.split(","));
        return new CustomShadedJedis(shardedJedisPool);
    }


    @Singleton
    @Provides
    @Named("managerSessionRedis")
    public CustomShadedJedis managerSessionRedis(JedisPoolConfig jedisPoolConfig, @Named("redis.session.managerRedisServers") String managerRedisServer) {
        ShardedJedisPool shardedJedisPool = this.shardedJedisPool(jedisPoolConfig, managerRedisServer.split(","));
        return new CustomShadedJedis(shardedJedisPool);
    }

    @Singleton
    @Inject
    private JedisPoolConfig jedisPoolConfig(@Named("redis.pool.maxTotal") int maxTotal,
                                            @Named("redis.pool.maxIdle") int maxIdle,
                                            @Named("redis.pool.minIdle") int minIdle,
                                            @Named("redis.pool.maxWaitMillis") int maxWaitMillis,
                                            @Named("redis.pool.lifo") boolean lifo,
                                            @Named("redis.pool.testOnBorrow") boolean testOnBorrow) {
        // Jedis池配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal); // 最大分配的对象数
        config.setMaxIdle(maxIdle); // 最大能够保持idel状态的对象数
        config.setMinIdle(minIdle); // 最小空闲的对象数。2.5.1以上版本有效
        config.setMaxWaitMillis(maxWaitMillis); // 当池内没有返回对象时，最大等待时间
        config.setLifo(lifo); // 是否启用Lifo。如果不设置，默认为true。2.5.1以上版本有效
        config.setTestOnBorrow(testOnBorrow); // 当调用borrow Object方法时，是否进行有效性检查
        return config;
    }

    private ShardedJedisPool shardedJedisPool(JedisPoolConfig jedisPoolConfig, String... hosts) {
        // map host
        List<JedisShardInfo> jedisInfoList = new ArrayList<>(hosts.length);
        // loop
        for (String host : hosts) {
            JedisShardInfo jedisShardInfo = new JedisShardInfo(host);
            jedisShardInfo.setConnectionTimeout(2000);
            jedisShardInfo.setSoTimeout(2000);
            jedisInfoList.add(jedisShardInfo);
        }
        // return ShardedJedisPool
        return new ShardedJedisPool(jedisPoolConfig, jedisInfoList);
    }
}
