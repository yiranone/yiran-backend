package one.yiran.dashboard.manage.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@ConditionalOnProperty(name = "dashboard.cache",havingValue = "local")
@Configuration
public class GuavaCacheConfig {

    @Bean
    public Cache cache() {
        Cache<String,String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES).
                maximumSize(10000).build();
        return cache;
    }
}