package com.block.chain.utils.redis;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * RedissonRpcAutoConfiguration
 *
 * @author zgy
 */
@Configuration
public class RedissonConfig {

    private final static Logger logger = LoggerFactory.getLogger(RedissonConfig.class);

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        logger.info("RedissonConfig>>> START");
        RedisProperties.Pool pool = redisProperties.getLettuce().getPool();

        String password = redisProperties.getPassword();
        if (StringUtils.isBlank(password)) {
            password = null;
        }

        long timeout = Objects.nonNull(redisProperties.getTimeout()) ? redisProperties.getTimeout().toMillis() : TimeUnit.SECONDS.toMillis(10);

        String prefix = redisProperties.isSsl() ? "rediss://" : "redis://";
        Config config = new Config();
        config.useSingleServer()
                .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setConnectTimeout((int) timeout)
                .setDatabase(redisProperties.getDatabase())
                .setPassword(password)
                .setDatabase(1)
                .setConnectionPoolSize(pool.getMaxIdle())
                .setConnectionMinimumIdleSize(pool.getMinIdle());
        return Redisson.create(config);
    }
}