package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysAnalysisItem;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisItemDao extends BaseDao<SysAnalysisItem, Long> {

}
