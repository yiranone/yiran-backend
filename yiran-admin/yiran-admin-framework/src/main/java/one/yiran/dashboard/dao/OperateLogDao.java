package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysOperateLog;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

@Repository
public interface OperateLogDao extends BaseDao<SysOperateLog, Long> {
}
