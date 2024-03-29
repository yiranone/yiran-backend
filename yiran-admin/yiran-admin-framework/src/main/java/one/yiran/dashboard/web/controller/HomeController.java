package one.yiran.dashboard.web.controller;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.expection.user.UserNotLoginException;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.entity.SysMenu;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.service.SysMenuService;
import one.yiran.dashboard.service.SysPermService;
import one.yiran.dashboard.web.model.WebMenu;
import one.yiran.dashboard.web.model.WebPerm;
import one.yiran.dashboard.web.util.MenuUtil;
import one.yiran.dashboard.web.util.PermUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class HomeController {

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysPermService sysPermService;

    //后台首页菜单获取接口，树形
    @AjaxWrapper
    @RequestMapping("/menu")
    public List<WebMenu> menu(ModelMap model) {
        UserSession user = SessionContextHelper.getLoginUser();
        if(user == null)
            throw new UserNotLoginException();
        List<SysMenu> menusList;
        if (user.isAdmin()) {
            menusList = sysMenuService.selectVisibleTreeMenus(true);
        } else {
            menusList = sysMenuService.selectVisibleTreeMenusByUser(user.getUserId(),true);
        }
        return MenuUtil.toWebMenu(menusList);
    }

    /**
     * 权限树角色授权的时候用，所有的
     * @return
     */
    @AjaxWrapper
    @RequestMapping("/perms")
    public List<WebPerm> perms() {
        UserSession user = SessionContextHelper.getLoginUser();
        if(user == null)
            throw new UserNotLoginException();
        List<SysMenu> menusList;
        if (user.isAdmin()) {
            menusList = sysMenuService.selectVisibleTreeMenus(false);
        } else {
            menusList = sysMenuService.selectVisibleTreeMenusByUser(user.getUserId(),false);
        }
        return PermUtil.toWebPerm(menusList);
    }
}