package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysOperateLog;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

@Repository
public interface OperateLogDao extends BaseDao<SysOperateLog, Long> {
}
