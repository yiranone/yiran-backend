package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysDept;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

@Repository
public interface DeptDao extends BaseDao<SysDept, Long> {


}
