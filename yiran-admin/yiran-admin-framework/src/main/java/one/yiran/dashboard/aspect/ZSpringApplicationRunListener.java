package one.yiran.dashboard.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.LocalDateTime;

@Slf4j
public class ZSpringApplicationRunListener implements SpringApplicationRunListener {

    public ZSpringApplicationRunListener(SpringApplication sa, String[] args) {
    }
    @Override
    public void starting() {
        log.info("starting {}", LocalDateTime.now());
    }
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        log.info("environmentPrepared {}", LocalDateTime.now());
    }
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        log.info("contextPrepared {}", LocalDateTime.now());
    }
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        log.info("contextLoaded {}", LocalDateTime.now());
    }
    @Override
    public void started(ConfigurableApplicationContext context) {
        log.info("started {}", LocalDateTime.now());
    }
    @Override
    public void running(ConfigurableApplicationContext context) {
        log.info("running {}", LocalDateTime.now());
    }
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        log.info("failed {}", LocalDateTime.now());
    }
}