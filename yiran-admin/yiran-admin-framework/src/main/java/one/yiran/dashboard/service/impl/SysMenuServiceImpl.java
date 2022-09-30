package one.yiran.dashboard.service.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.Predicate;
import one.yiran.common.domain.PageRequest;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.entity.QSysMenu;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.service.SysMenuService;
import one.yiran.dashboard.dao.MenuDao;
import one.yiran.dashboard.entity.SysMenu;
import one.yiran.dashboard.service.SysRoleService;
import one.yiran.db.common.service.CrudBaseServiceImpl;
import one.yiran.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static one.yiran.dashboard.common.constants.SystemConstants.MENU_IS_FRAME;
import static one.yiran.dashboard.common.constants.SystemConstants.MENU_NOT_FRAME;

@Service
public class SysMenuServiceImpl extends CrudBaseServiceImpl<Long, SysMenu> implements SysMenuService {

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private SysRoleService sysRoleService;

    //查询某个用户已经有的权限 onlyMenu=true只返回菜单属性的内容
    @Override
    public List<SysMenu> selectVisibleTreeMenusByUser(Long userId, boolean onlyMenu) {
        Assert.notNull(userId, "用户ID不能为空");
        List<String> permList = sysRoleService.findUserPermsByUserId(userId);
        if(permList == null)
            permList = new ArrayList<>();
        List<SysMenu> sysMenus = filterHomePageMenu(onlyMenu);
        if(onlyMenu) {
            List<String> finalPermList = permList;
            sysMenus = sysMenus.stream().filter(e ->
                    StringUtils.isBlank(e.getPerms()) || finalPermList.contains(e.getPerms())
            ).collect(toList());
        }
        sortMenus(sysMenus);
        List<SysMenu> tree = toTree(sysMenus, 0);
        tree = tree.stream().filter(e ->
                !StringUtils.equals(e.getMenuType(), "M") ||
                e.getChildren() != null && e.getChildren().size() > 0).collect(toList());
        return tree;
    }

    @Override
    public List<SysMenu> selectVisibleTreeMenus(boolean onlyMenu) {
        List<SysMenu> sysMenus = filterHomePageMenu(onlyMenu);
        sortMenus(sysMenus);
        List<SysMenu> tree = toTree(sysMenus, 0);
        //目录下面没有菜单就不显示
        tree = tree.stream().filter(e ->
                !StringUtils.equals(e.getMenuType(), "M") ||
                        e.getChildren() != null && e.getChildren().size() > 0).collect(toList());
        return tree;
    }

    private List<SysMenu> filterHomePageMenu(boolean onlyMenu) {
        List<SysMenu> sysMenus = menuDao.findAll();
        if(onlyMenu) {
            sysMenus = sysMenus.stream().filter(e -> !StringUtils.equals(e.getMenuType(),"F")).collect(Collectors.toList());
        }
        sysMenus = sysMenus.stream().filter(e -> StringUtils.equals(SystemConstants.MENU_VISIBLE, e.getVisible()))
                    .filter(e -> e.getIsDelete() == null || !e.getIsDelete().booleanValue()).collect(toList());


        return sysMenus;
    }

    @Override
    public List<SysMenu> selectMenuList(PageRequest request, List<Predicate> predicates) {
        List<SysMenu> results = selectList(request, predicates, QSysMenu.sysMenu.orderNum, Order.ASC);
        sortMenus(results);
        return results;
    }

    @Transactional
    @Override
    public long remove(Long menuId) throws BusinessException {
        if (countMenuByParentId(menuId) > 0) {
            throw BusinessException.build("存在子菜单,不允许删除");
        }
        return super.remove(menuId);
    }

    @Override
    public int countMenuByParentId(Long parentId) {
        return menuDao.countByParentId(parentId);
    }

    @Transactional
    @Override
    public int insertMenu(SysMenu sysMenu) {
        checkMenuValid(sysMenu);
        if (sysMenu.getMenuId() != null) {
            throw BusinessException.build("新建菜单 menuId不能有值:" + sysMenu.getMenuId());
        }
        sysMenu.setCreateBy(SessionContextHelper.getCurrentLoginName());
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
        sysMenu.setCreateBy(SessionContextHelper.getCurrentLoginName());
        sysMenu.setUpdateBy(SessionContextHelper.getCurrentLoginName());
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
            if(StringUtils.equals(m.getMenuType(), "C") ){
                if (StringUtils.isBlank(m.getIsFrame())) {
                    throw BusinessException.build("是否外链不能为空");
                }
                if (StringUtils.isBlank(m.getRouter())) {
                    throw BusinessException.build("菜单路由地址不能为空");
                }
                if (StringUtils.equals(m.getIsFrame(),MENU_NOT_FRAME) && StringUtils.isBlank(m.getComponent())) {
                    throw BusinessException.build("菜单组件不能为空");
                }
                if(StringUtils.equals(m.getIsFrame(),MENU_IS_FRAME)) {
                    if (!StringUtils.startsWith(m.getRouter(), "http")) {
                        throw BusinessException.build("是链接的时候路由地址必须以http开头");
                    }
                }
            }
            if (StringUtils.equals(m.getMenuType(), "M")) {
                m.setIsFrame(MENU_NOT_FRAME);
            }
//            if (StringUtils.equals(m.getMenuType(), "C") && StringUtils.isBlank(m.getTarget())) {
//                throw BusinessException.build("打开方式不能为空");
//            }
        } else if (StringUtils.equals(m.getMenuType(), "F")) {
            if(StringUtils.isBlank(m.getIsFrame())) {
                throw BusinessException.build("是否是链接不能为空");
            }
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

    private void sortMenus(List<SysMenu> sysMenus) {
        if (sysMenus == null || sysMenus.size() == 0)
            return;
        Collections.sort(sysMenus, (o1, o2) -> {
            if (o1.getParentId() == null && o2.getParentId() == null) {
                return o1.getOrderNum() - o2.getOrderNum();
            } else if (o1.getParentId() == null) {
                return -1;
            } else if (o2.getParentId() == null) {
                return 1;
            } else if (o1.getParentId().longValue() == o2.getParentId().longValue()) {
                return o1.getOrderNum() - o2.getOrderNum();
            } else {
                return o1.getParentId().intValue() - o2.getParentId().intValue();
            }
        });
    }
}
