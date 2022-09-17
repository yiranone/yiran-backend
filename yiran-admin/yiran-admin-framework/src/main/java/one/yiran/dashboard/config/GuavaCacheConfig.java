package one.yiran.dashboard.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.constants.Global;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@ConditionalOnProperty(name = "dashboard.cache",havingValue = "local")
@Configuration
@Slf4j
public class GuavaCacheConfig {

    @Bean
    public Cache cache() {
        long timeout = Global.getSessionTimeout().longValue();
        log.info("设置本地Cache session有效期{}min",timeout);
        Cache<String,String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(timeout, TimeUnit.MINUTES).
                maximumSize(10000).build();
        return cache;
    }
}