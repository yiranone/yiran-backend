package one.yiran.dashboard.manage.service.impl;

import one.yiran.dashboard.manage.entity.QSysConfig;
import one.yiran.dashboard.manage.dao.ConfigDao;
import one.yiran.dashboard.manage.entity.SysConfig;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.dashboard.manage.service.SysConfigService;
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

    /**
     * 校验参数键名是否唯一
     *
     * @param sysConfig 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfig sysConfig) {
        Long configId = sysConfig.getConfigId() == null ? -1L : sysConfig.getConfigId();
        SysConfig info = configDao.findByConfigKey(sysConfig.getConfigKey());
        if (info != null && info.getConfigId().longValue() != configId.longValue()) {
            return false;
        }
        return true;
    }
}
