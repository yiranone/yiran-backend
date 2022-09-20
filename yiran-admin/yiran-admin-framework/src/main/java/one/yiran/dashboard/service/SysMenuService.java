package one.yiran.dashboard.service;

import com.querydsl.core.types.Predicate;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.entity.SysMenu;
import one.yiran.db.common.service.CrudBaseService;
import one.yiran.common.domain.Ztree;

import java.util.List;

public interface SysMenuService extends CrudBaseService<Long, SysMenu> {

    List<SysMenu> selectVisibleTreeMenus(boolean onlyMenu);

    List<SysMenu> selectVisibleTreeMenusByUser(Long userId,boolean onlyMenu);

    List<SysMenu> selectMenuList(PageRequest request, List<Predicate> predicates);

    /**
     * 查询菜单数量
     *
     * @param parentId 菜单父ID
     * @return 结果
     */
    int countMenuByParentId(Long parentId);

    /**
     * 新增保存菜单信息
     *
     * @param sysMenu 菜单信息
     * @return 结果
     */
    int insertMenu(SysMenu sysMenu);

    /**
     * 修改保存菜单信息
     *
     * @param sysMenu 菜单信息
     * @return 结果
     */
    int updateMenu(SysMenu sysMenu);

    /**
     * 校验菜单名称是否唯一
     *
     * @param sysMenu 菜单信息
     * @return 结果
     */
    boolean checkMenuNameUnique(SysMenu sysMenu);
}
