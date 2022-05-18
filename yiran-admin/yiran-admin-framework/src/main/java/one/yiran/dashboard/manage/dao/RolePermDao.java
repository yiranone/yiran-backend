package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysRolePerm;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermDao extends BaseDao<SysRolePerm, Long> {

    long deleteAllByRoleId(Long roleId);

    long deleteAllByRoleIdAndPermIdIn(Long roleId, List<Long> permIds);

    long countByRoleIdAndPermId(Long roleId, Long permId);

    int countByPermId(Long permId);

    List<SysRolePerm> findAllByPermId(Long permId);

    List<SysRolePerm> findAllByRoleId(Long roleId);
}
