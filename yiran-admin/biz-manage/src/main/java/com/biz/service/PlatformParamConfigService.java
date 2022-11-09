package com.biz.service;


import com.biz.entity.PlatformParamConfig;
import one.yiran.db.common.service.CrudBaseService;

import java.util.List;

public interface PlatformParamConfigService extends CrudBaseService<Long,PlatformParamConfig> {

    PlatformParamConfig saveOrUpdate(PlatformParamConfig platformParamConfig);

    PlatformParamConfig findById(Long id);
    List<PlatformParamConfig> findByConfigKey(String configKey);

}
