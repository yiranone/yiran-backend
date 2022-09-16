package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysRole;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleDao extends BaseDao<SysRole, Long> {


    SysRole findByRoleName(String roleName);

    SysRole findByRoleKey(String roleKey);

    SysRole findByRoleId(Long roleId);

    List<SysRole> findAllByRoleIdIn(List<Long> roleId);

    List<SysRole> findAllByRoleIdIn(Long[] ids);
}
