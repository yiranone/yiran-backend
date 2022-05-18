package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysOperLog;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperLogDao extends BaseDao<SysOperLog, Long> {

    SysOperLog findByOperId(Long operId);

    int deleteByOperIdIn(List<Long> delIds);
}
