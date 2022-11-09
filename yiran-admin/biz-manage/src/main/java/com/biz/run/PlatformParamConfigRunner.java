package com.biz.run;

import com.biz.constants.PlatformConfigEnum;
import com.biz.dao.PlatformParamConfigDao;
import com.biz.entity.PlatformParamConfig;
import com.biz.service.PlatformParamConfigService;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.util.MemberCacheUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PlatformParamConfigRunner implements ApplicationRunner {

    @Resource
    private ConfigurableEnvironment environment;
    @Resource
    private PlatformParamConfigDao platformParamConfigDao;
    @Resource
    private PlatformParamConfigService platformParamConfigService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        PlatformConfigEnum[] vs = PlatformConfigEnum.values();
        for (PlatformConfigEnum e : vs) {
            String key = e.name();
            if(StringUtils.isBlank(key))
                continue;
            List<PlatformParamConfig> lists = platformParamConfigService.findByConfigKey(key);
            boolean isExistNull = false;
            for(PlatformParamConfig c:lists ){
                if(c.getChannelId() == null)
                    isExistNull = true;
            }
            if(!isExistNull) {
                PlatformParamConfig c = new PlatformParamConfig();
                c.setConfigKey(key);
                c.setConfigValue("");
                c.setConfigGroup("sys");
                c.setCreateBy("sys");
                c.setCreateTime(new Date());
                c.setDescription("初始化,没有设置");
                platformParamConfigService.insert(c);
            }
        }
        //设置各家商户缓存数据
        platformParamConfigDao.findAll().stream().forEach(dd -> {
            String key = dd.getConfigKey();
            if (dd.getChannelId() != null) {
                key = dd.getChannelId() + "_" + dd.getConfigKey();
            }
            log.info("加载系统缓存 {}->{}", key, dd.getConfigValue());
            MemberCacheUtil.setSystemConfig(key, dd.getConfigValue());
        });

//        log.info("************************* SystemProperties ******************************");
//        Map<String, Object> env1 = environment.getSystemProperties();
//        for (String envName : env1.keySet()) {
//            log.info(">>> {}={}",
//                    envName,
//                    env1.get(envName));
//        }
//
//        log.info("************************* SystemEnvironment ******************************");
//        Map<String, Object> env2 = environment.getSystemEnvironment();
//        for (String envName : env2.keySet()) {
//            log.info(">>> {}={}",
//                    envName,
//                    env2.get(envName));
//        }
//
//        log.info("************************* ACTIVE APP PROPERTIES ******************************");
//
//        List<MapPropertySource> propertySources = new ArrayList<>();
//
//        environment.getPropertySources().forEach(it -> {
//            if (it instanceof MapPropertySource && it.getName().contains("applicationConfig")) {
//                propertySources.add((MapPropertySource) it);
//            }
//        });
//
//        propertySources.stream()
//                .map(propertySource -> propertySource.getSource().keySet())
//                .flatMap(Collection::stream)
//                .distinct()
//                .sorted()
//                .forEach(key -> {
//                    try {
//                        log.info(key + "=" + environment.getProperty(key));
//                    } catch (Exception e) {
//                        log.warn(">> {} -> {}", key, e.getMessage());
//                    }
//                });
        log.info("******************************************************************************");
    }
}
