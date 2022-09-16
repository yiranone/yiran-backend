package one.yiran.dashboard.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@ConditionalOnProperty(name = "dashboard.cache",havingValue = "redis")
@Configuration
@Slf4j
public class RadisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.database}")
    private int database;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdel;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdel;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private int maxWait;

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(maxIdel);
        poolConfig.setMinIdle(minIdel);
        poolConfig.setMaxTotal(maxActive);
        //当池内没有可用的连接时，最大等待时间
        poolConfig.setMaxWaitMillis(1000);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(50000); //多久空闲就移除
        poolConfig.setTimeBetweenEvictionRunsMillis(30000); //检测间隔时间
        poolConfig.setNumTestsPerEvictionRun(-1); //检测所有链接
        return poolConfig;
    }

    @Bean
    public JedisPool jedisPool(JedisPoolConfig poolConfig) {
        JedisPool jedisPool = new JedisPool(poolConfig,
                host, port, timeout, password, database, "sp-back");
        return jedisPool;
    }
}

