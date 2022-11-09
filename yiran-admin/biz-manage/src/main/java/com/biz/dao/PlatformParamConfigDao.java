package com.biz.dao;

import com.biz.entity.PlatformParamConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformParamConfigDao extends JpaRepository<PlatformParamConfig, Long>,
        JpaSpecificationExecutor<PlatformParamConfig> {

    PlatformParamConfig findFirstByChannelIdAndConfigKey(Long channelId, String key);

    List<PlatformParamConfig> findAllByChannelId(Long channelId);

    PlatformParamConfig findFirstByConfigKey(String configKey);
}
