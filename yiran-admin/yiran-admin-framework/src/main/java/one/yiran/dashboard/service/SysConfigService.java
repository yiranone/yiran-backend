package one.yiran.dashboard.service;

import one.yiran.dashboard.entity.SysConfig;
import one.yiran.db.common.service.CrudBaseService;

public interface SysConfigService extends CrudBaseService<Long, SysConfig> {

    /**
     * 只查询正常的config
     * @param key
     * @return
     */
    String selectNormalConfigByKey(String key);

    String selectConfigByKey(String configKey);

    boolean checkConfigKeyUnique(String configKey,Long configId);
}
