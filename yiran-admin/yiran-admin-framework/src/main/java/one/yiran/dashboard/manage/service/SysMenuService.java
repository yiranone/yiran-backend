package one.yiran.dashboard.manage.service;

import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.manage.entity.SysMenu;
import one.yiran.db.common.service.CrudBaseService;
import one.yiran.common.domain.Ztree;

import java.util.List;

public interface SysMenuService extends CrudBaseService<Long, SysMenu> {

    List<SysMenu> selectVisibleMenus();

    List<SysMenu> selectVisibleMenusByUser(Long userId);

    List<SysMenu> selectMenuList(PageRequest request, SysMenu sysMenu);

    /**
     * 查询所有菜单信息
     *
     * @return 菜单列表
     */
    List<Ztree> menuTreeData();

    /**
     * 查询菜单数量
     *
     * @param parentId 菜单父ID
     * @return 结果
     */
    int selectCountMenuByParentId(Long parentId);

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
