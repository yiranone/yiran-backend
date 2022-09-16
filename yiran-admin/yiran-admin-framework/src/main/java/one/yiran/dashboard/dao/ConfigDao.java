package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysConfig;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigDao extends BaseDao<SysConfig, Long> {

    SysConfig findByConfigId(Long configId);

    SysConfig findByConfigKey(String configKey);

    int deleteAllByConfigIdIn(List<Long> delIds);
}
