package one.yiran.dashboard.dao;

import one.yiran.dashboard.entity.SysMenu;
import one.yiran.db.common.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuDao extends BaseDao<SysMenu, Long> {

    SysMenu findByMenuId(Long menuId);

    SysMenu findByMenuName(String menuName);

    List<SysMenu> findAllByParentId(Long parentId);

    int countByParentId(Long parentId);

    List<SysMenu> findAllByMenuIdIn(List<Long> menuIds);
}
