package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysUserOnline;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOnlineDao extends BaseDao<SysUserOnline, String> {

    SysUserOnline findBySessionId(String sessionId);

}
