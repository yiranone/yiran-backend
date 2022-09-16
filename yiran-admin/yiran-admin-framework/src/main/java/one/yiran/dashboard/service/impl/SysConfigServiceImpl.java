package one.yiran.dashboard.service.impl;

import one.yiran.dashboard.entity.QSysConfig;
import one.yiran.dashboard.dao.ConfigDao;
import one.yiran.dashboard.entity.SysConfig;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.dashboard.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SysConfigServiceImpl extends CrudBaseServiceImpl<Long, SysConfig> implements SysConfigService {

    @Autowired
    private ConfigDao configDao;

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数名称
     * @return 参数键值
     */
    @Override
    public String selectNormalConfigByKey(String configKey) {
        QSysConfig qSysConfig = QSysConfig.sysConfig;
        Optional<SysConfig> d = configDao.findOne(qSysConfig.configKey.eq(configKey).and((qSysConfig.isDelete).eq(Boolean.TRUE)).not());
        if(d.isPresent())
            return d.get().getConfigValue();
        return null;
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数名称
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey) {
        SysConfig sysConfig = configDao.findByConfigKey(configKey);
        if (sysConfig == null) {
            return null;
        }
        return sysConfig.getConfigValue();

    }

    @Override
    public boolean checkConfigKeyUnique(String configKey, Long configId) {
        SysConfig info = configDao.findByConfigKey(configKey);
        if (info != null && info.getConfigId().longValue() != configId.longValue()) {
            return false;
        }
        return true;
    }
}
