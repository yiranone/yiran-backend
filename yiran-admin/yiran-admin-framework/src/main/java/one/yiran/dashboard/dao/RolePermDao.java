package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysRolePerm;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermDao extends BaseDao<SysRolePerm, Long> {

    @Modifying(flushAutomatically = true,clearAutomatically = true)
    long deleteAllByRoleId(Long roleId);

    @Modifying(flushAutomatically = true,clearAutomatically = true)
    long deleteAllByRoleIdAndPermIdIn(Long roleId, List<Long> permIds);

    long countByRoleIdAndPermId(Long roleId, Long permId);

    int countByPermId(Long permId);

    List<SysRolePerm> findAllByPermId(Long permId);

    List<SysRolePerm> findAllByRoleId(Long roleId);
}
