package com.biz.service.impl;

import com.biz.dao.PlatformParamConfigDao;
import com.biz.entity.PlatformParamConfig;
import com.biz.entity.QPlatformParamConfig;
import com.biz.service.PlatformParamConfigService;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.util.MemberCacheUtil;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PlatformParamConfigServiceImpl extends CrudBaseServiceImpl<Long,PlatformParamConfig> implements PlatformParamConfigService {

    @Autowired
    private PlatformParamConfigDao platformParamConfigDao;

    @Override
    @Transactional
    public PlatformParamConfig saveOrUpdate(PlatformParamConfig platformParamConfig) {
//		if (StringUtils.isBlank(platformParamConfig.getChannelCode())) {
//			throw BusinessException.build("数据异常，未关联商户，请连续管理员");
//		}
        String key = "";
        if (platformParamConfig.getChannelId() != null) {
            key = platformParamConfig.getChannelId() + "_" + platformParamConfig.getConfigKey();
        } else {
            key = platformParamConfig.getConfigKey();
        }
        PlatformParamConfig dbConfig = null;
        if (platformParamConfig.getChannelId() == null) {
            dbConfig = platformParamConfigDao.findFirstByConfigKey(platformParamConfig.getConfigKey());
        } else {
            dbConfig = platformParamConfigDao.findFirstByChannelIdAndConfigKey(
                    platformParamConfig.getChannelId(), platformParamConfig.getConfigKey());
        }
        if (platformParamConfig.getId() == null && dbConfig != null) {
            throw BusinessException.build("新增数据异常，数据库已经存在 key=" + platformParamConfig.getConfigKey());
        }
        if (dbConfig == null) {
            if (platformParamConfig.getId() == null)
                dbConfig = new PlatformParamConfig();
            else
                throw BusinessException.build("修改数据异常，数据库不存在 key=" + platformParamConfig.getConfigKey());
        }
        if (StringUtils.equals(dbConfig.getValueType(), "Long")) {
            try {
                Long.valueOf(platformParamConfig.getConfigValue());
            } catch (Exception e) {
                throw BusinessException.build("数据异常，类型必须为整数Long:" + platformParamConfig.getConfigKey());
            }
        }
        dbConfig.setChannelId(platformParamConfig.getChannelId());
        dbConfig.setConfigGroup(platformParamConfig.getConfigGroup());
        dbConfig.setConfigKey(platformParamConfig.getConfigKey());
        dbConfig.setConfigName(platformParamConfig.getConfigName());
        dbConfig.setConfigValue(platformParamConfig.getConfigValue());
        dbConfig.setDescription(platformParamConfig.getDescription());

        if(platformParamConfig.getId() == null) {
            dbConfig.setCreateTime(new Date());
            dbConfig.setCreateBy(SessionContextHelper.getCurrentLoginName());
        }
        dbConfig.setUpdateBy(SessionContextHelper.getCurrentLoginName());
        dbConfig.setUpdateTime(new Date());

        platformParamConfigDao.save(dbConfig);
        log.info("清理系统缓存key {}", key);
        MemberCacheUtil.removeSystemConfig(key);
        MemberCacheUtil.setSystemConfig(key, dbConfig.getConfigValue());
        return dbConfig;
    }

    @Override
    public PlatformParamConfig findById(Long id) {
        return platformParamConfigDao.findById(id).orElseThrow(()-> BusinessException.build("找不到参数配置" + id));
    }

    @Override
    public List<PlatformParamConfig> findByConfigKey(String configKey) {
        Assert.notNull(configKey,"configKey不能为空");
        return selectList(QPlatformParamConfig.platformParamConfig.configKey.eq(configKey));
    }

}
