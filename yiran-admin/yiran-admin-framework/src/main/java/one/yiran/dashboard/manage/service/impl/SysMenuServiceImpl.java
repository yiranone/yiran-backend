package one.yiran.dashboard.manage.service.impl;

import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.manage.service.SysMenuService;
import one.yiran.dashboard.manage.service.SysPermService;
import one.yiran.dashboard.manage.dao.MenuDao;
import one.yiran.dashboard.manage.entity.SysMenu;
import one.yiran.dashboard.manage.entity.SysPerm;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.common.exception.BusinessException;
import one.yiran.common.domain.Ztree;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class SysMenuServiceImpl extends CrudBaseServiceImpl<Long, SysMenu> implements SysMenuService {

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private SysPermService sysPermService;

    @Override
    public List<SysMenu> selectVisibleTreeMenusByUser(Long userId,boolean onlyMenu) {
        Assert.notNull(userId, "用户ID不能为空");
        List<SysPerm> permList = sysPermService.findPermsByUserId(userId);
        List<String> permOperationList = permList.stream().map(e -> e.getPermOperation()).collect(toList());
        List<SysMenu> sysMenus = menuDao.findAll();
        if (sysMenus != null && sysMenus.size() > 0) {
            if(onlyMenu) {
                sysMenus = sysMenus.stream().filter(e -> !StringUtils.equals(e.getMenuType(),"F")).collect(Collectors.toList());
            }
            sysMenus = sysMenus.stream().filter(e -> permOperationList.contains(e.getPerms()))
                    .filter(e -> StringUtils.equals("0", e.getVisible()))
                    .filter(e -> e.getIsDelete() == null || !e.getIsDelete().booleanValue()).collect(toList());
        }
        sortMenus(sysMenus);
        return toTree(sysMenus, 0);
    }

    @Override
    public List<SysMenu> selectVisibleTreeMenus(boolean onlyMenu) {
        List<SysMenu> sysMenus = menuDao.findAll();
        if(onlyMenu) {
            sysMenus = sysMenus.stream().filter(e -> !StringUtils.equals(e.getMenuType(),"F")).collect(Collectors.toList());
        }
        if(sysMenus != null && sysMenus.size() > 0) {
            sysMenus = sysMenus.stream().filter(e -> StringUtils.equals("0", e.getVisible()))
                    .filter(e -> e.getIsDelete() == null || !e.getIsDelete().booleanValue()).collect(toList());
        }
        sortMenus(sysMenus);
        return toTree(sysMenus, 0);
    }

    @Override
    public List<SysMenu> selectMenuList(PageRequest request, SysMenu sysMenu) {
        List<SysMenu> results = selectList(request, sysMenu);
        sortMenus(results);
        return results;
    }

    @Transactional
    @Override
    public long remove(Long menuId) throws BusinessException {
        if (selectCountMenuByParentId(menuId) > 0) {
            throw BusinessException.build("存在子菜单,不允许删除");
        }
        return super.remove(menuId);
    }

    /**
     * 查询所有菜单
     *
     * @return 菜单列表
     */
    @Override
    public List<Ztree> menuTreeData() {
        List<SysMenu> sysMenuList = menuDao.findAll();

        SysMenu root = new SysMenu();
        root.setMenuId(0L);
        root.setMenuName("根目录");
        root.setPerms("");
        sysMenuList.add(root);

        sortMenus(sysMenuList);
        List<Ztree> ztrees = initZtree(sysMenuList);
        return ztrees;
    }

    /**
     * 对象转菜单树
     *
     * @param sysMenuList 菜单列表
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> sysMenuList) {
        return initZtree(sysMenuList, null, false);
    }

    /**
     * 对象转菜单树
     *
     * @param sysMenuList  菜单列表,所有的菜单
     * @param roleMenuList 角色已存在菜单列表，部分菜单
     * @param permsFlag    是否需要显示权限标识
     * @return 树结构列表
     */
    public List<Ztree> initZtree(List<SysMenu> sysMenuList, List<Long> roleMenuList, boolean permsFlag) {
        List<Ztree> ztrees = new ArrayList<>();
        for (SysMenu sysMenu : sysMenuList) {
            Ztree ztree = new Ztree();
            ztree.setId(sysMenu.getMenuId());
            ztree.setPId(sysMenu.getParentId());
            ztree.setName(transMenuName(sysMenu, permsFlag));
            //ztree.setTitle(sysMenu.getMenuName());
            if (roleMenuList != null) {
                ztree.setChecked(roleMenuList.contains(sysMenu.getMenuId()));
            }
            ztrees.add(ztree);
        }
        return ztrees;
    }

    public String transMenuName(SysMenu sysMenu, boolean permsFlag) {
        StringBuffer sb = new StringBuffer();
        sb.append(sysMenu.getMenuName());
        if (permsFlag) {
            sb.append("<font color=\"#888\">&nbsp;&nbsp;&nbsp;" + sysMenu.getPerms() + "</font>");
        }
        return sb.toString();
    }


    @Override
    public int selectCountMenuByParentId(Long parentId) {
        return menuDao.countByParentId(parentId);
    }

    @Transactional
    @Override
    public int insertMenu(SysMenu sysMenu) {
        checkMenuValid(sysMenu);
        if (sysMenu.getMenuId() != null) {
            throw BusinessException.build("新建菜单 menuId不能有值:" + sysMenu.getMenuId());
        }
        super.insert(sysMenu);
        return 1;
    }

    @Transactional
    @Override
    public int updateMenu(SysMenu sysMenu) {
        Assert.notNull(sysMenu, "");
        Assert.notNull(sysMenu.getMenuId(), "menuId不能未空");
        SysMenu dbSysMenu = menuDao.findByMenuId(sysMenu.getMenuId());
        if (dbSysMenu != null) {
            sysMenu.setMenuId(dbSysMenu.getMenuId());
        } else {
            throw BusinessException.build("菜单未找到 menuId=" + sysMenu.getMenuId());
        }
        checkMenuValid(sysMenu);
        update(sysMenu);
        return 1;
    }

    private void checkMenuValid(SysMenu m) throws BusinessException {
        if (!checkMenuNameUnique(m))
            throw BusinessException.build("菜单名称重复");

        if (StringUtils.isBlank(m.getMenuType())) {
            throw BusinessException.build("菜单类型不能为空");
        }
        if (StringUtils.equalsAny(m.getMenuType(), "C", "F") && StringUtils.isBlank(m.getPerms())) {
            throw BusinessException.build("菜单和按钮的权限标识不能为空");
        }
        if(m.getOrderNum() == null){
            throw BusinessException.build("显示排序不能为空");
        }
        if (!StringUtils.equalsAny(m.getMenuType(), "M", "C", "F")) {
            throw BusinessException.build("菜单类型不正确");
        }
        if (StringUtils.equalsAny(m.getMenuType(), "M", "C")) {
            if (StringUtils.isBlank(m.getVisible())) {
                throw BusinessException.build("菜单菜单状态不能为空");
            }
            if (StringUtils.equals(m.getMenuType(), "C") && StringUtils.isBlank(m.getRouter())) {
                throw BusinessException.build("菜单router不能为空");
            }
//            if (StringUtils.equals(m.getMenuType(), "C") && StringUtils.isBlank(m.getTarget())) {
//                throw BusinessException.build("打开方式不能为空");
//            }
        } else if (StringUtils.equals(m.getMenuType(), "F")) {
            m.setRouter("");
        }

        if (m.getParentId() != null && m.getParentId().longValue() > 0) {
            SysMenu parentMenu = menuDao.findByMenuId(m.getParentId());
            if (parentMenu == null) {
                throw BusinessException.build("父菜单不存在");
            }
            if (parentMenu.getMenuId().equals(m.getMenuId())) {
                throw BusinessException.build("当前菜单的父菜单不能是自己");
            }
            if (StringUtils.equalsAny(m.getMenuType(), "M", "C")) {
                if (!StringUtils.equals(parentMenu.getMenuType(), "M")) {
                    throw BusinessException.build("父菜单不是目录，不能选择");
                }
            } else if (StringUtils.equals(m.getMenuType(), "F")) {
                if (StringUtils.equals(parentMenu.getMenuType(), "F")) {
                    throw BusinessException.build("父菜单不是目录或者菜单，不能选择");
                }
            }
        }

        if (m.getMenuId() != null) {
            List<SysMenu> childs = menuDao.findAllByParentId(m.getMenuId());
            if (childs != null && childs.size() > 0) {
                if (!StringUtils.equalsAny(m.getMenuType(), "M", "C")) {
                    throw BusinessException.build("菜单有子成员，类型只能新增或修改为目录或者菜单，不能修改为其他类型");
                }
            }
        }

    }

    @Override
    public boolean checkMenuNameUnique(SysMenu sysMenu) {
        Assert.notNull(sysMenu, "sysMenu 不能为空");
        Assert.notNull(sysMenu.getMenuName(), "menuName 不能为空");
        Long parentId = sysMenu.getParentId() == null ? 0L : sysMenu.getParentId();
        SysMenu m = menuDao.findByMenuName(sysMenu.getMenuName());
        if (m != null && m.getMenuId().equals(sysMenu.getMenuId())) {
            return true;
        } else if (m != null && m.getParentId() != null && !m.getParentId().equals(parentId)) {
            return false;
        }
        return true;
    }

    public static List<SysMenu> getAllChildList(List<SysMenu> list, long parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu t = iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() != null && t.getParentId() == parentId) {
                recursionFn(list, t, returnList);
                if (!returnList.contains(t))
                    returnList.add(t);
            }
        }
        return returnList;
    }

    private static void recursionFn(List<SysMenu> list, SysMenu t, List<SysMenu> returnList) {
        // 得到子节点列表
        List<SysMenu> childList = getDirectChildList(list, t);
        if (childList == null || childList.size() == 0)
            return;
        t.setChildren(childList);
        childList.forEach(e -> {
            if (!returnList.contains(e))
                returnList.add(e);
        });
        for (SysMenu tChild : childList) {
            recursionFn(list, tChild, returnList);
        }
    }

    /**
     * 得到子节点列表
     */
    private static List<SysMenu> getDirectChildList(List<SysMenu> list, SysMenu t) {

        List<SysMenu> tlist = new ArrayList<>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext()) {
            SysMenu n = it.next();
            if (n.getParentId() != null && n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    public static List<SysMenu> toTree(List<SysMenu> list, long parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu t = iterator.next();
            if (t.getParentId() != null && t.getParentId() == parentId) {
                recursionTreeFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    private static void recursionTreeFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getDirectChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            recursionTreeFn(list, tChild);
        }
    }

    /**
     * 判断是否有子节点
     */
    private static boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getDirectChildList(list, t).size() > 0 ? true : false;
    }

    private void sortMenus(List<SysMenu> sysMenus) {
        if (sysMenus == null || sysMenus.size() == 0)
            return;
        Collections.sort(sysMenus, (o1, o2) -> {
            if (o1.getParentId() == null) {
                return -1;
            } else if (o2.getParentId() == null) {
                return 1;
            }
            if (o1.getParentId() == o2.getParentId()) {
                return o1.getOrderNum() - o2.getOrderNum();
            } else if (o1.getParentId() > o2.getParentId()) {
                return 1;
            } else {
                return -1;
            }
        });
    }
}
