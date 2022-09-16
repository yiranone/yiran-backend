package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysPerm;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermDao extends BaseDao<SysPerm, Long> {

    SysPerm findByPermName(String permName);

    SysPerm findByPermId(Long permId);

    List<SysPerm> findAllByOrderByPermSortAscPermIdAsc();
}
