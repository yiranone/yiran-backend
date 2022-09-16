package one.yiran.dashboard.web.service;

import one.yiran.dashboard.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("config")
public class ConfigService {

    @Autowired
    private SysConfigService sysConfigService;

    public String getKey(String key) {
        return  sysConfigService.selectNormalConfigByKey(key);
    }
}
