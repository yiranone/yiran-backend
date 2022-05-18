package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysConfig;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigDao extends BaseDao<SysConfig, Long> {

    SysConfig findByConfigId(Long configId);

    SysConfig findByConfigKey(String configKey);

    int deleteAllByConfigIdIn(List<Long> delIds);
}
