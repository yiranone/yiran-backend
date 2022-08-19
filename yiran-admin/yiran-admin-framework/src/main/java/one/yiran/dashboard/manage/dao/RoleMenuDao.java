package one.yiran.dashboard.manage.dao;

import one.yiran.dashboard.manage.entity.SysRoleMenu;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMenuDao extends BaseDao<SysRoleMenu, Long> {

    @Modifying(flushAutomatically = true,clearAutomatically = true)
    long deleteAllByRoleId(Long roleId);

    @Modifying(flushAutomatically = true,clearAutomatically = true)
    long deleteAllByRoleIdAndMenuIdIn(Long roleId, List<Long> menuIds);

    long countByRoleIdAndMenuId(Long roleId, Long menuId);

    int countByMenuId(Long menuId);

    List<SysRoleMenu> findAllByMenuId(Long menuId);

    List<SysRoleMenu> findAllByRoleId(Long roleId);
}
