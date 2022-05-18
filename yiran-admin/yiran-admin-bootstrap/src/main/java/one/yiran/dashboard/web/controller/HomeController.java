package one.yiran.dashboard.web.controller;

import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.expection.user.UserNotLoginException;
import one.yiran.dashboard.common.model.UserInfo;
import one.yiran.dashboard.common.util.StringUtil;
import one.yiran.dashboard.manage.entity.SysMenu;
import one.yiran.dashboard.manage.entity.SysPerm;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.service.SysMenuService;
import one.yiran.dashboard.manage.service.SysPermService;
import one.yiran.dashboard.manage.service.SysRoleService;
import one.yiran.dashboard.web.service.ConfigService;
import one.yiran.dashboard.web.model.WebMenu;
import one.yiran.dashboard.web.service.PermissionService;
import one.yiran.dashboard.web.util.MenuUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPermService sysPermService;

    @AjaxWrapper
    @RequestMapping("/menu")
    public List<WebMenu> menu(ModelMap model) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        if(user == null)
            throw new UserNotLoginException();
        List<SysMenu> menusList;
        if (user.isAdmin()) {
            menusList = sysMenuService.selectVisibleMenus();
        } else {
            menusList = sysMenuService.selectVisibleMenusByUser(user.getUserId());
        }
        return MenuUtil.toWebMenu(menusList);
    }

    @AjaxWrapper
    @RequestMapping("/perms")
    public List<Map<String, Object>> perms(ModelMap model) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        List<SysPerm> list;
        if (user.isAdmin()) {
            list = sysPermService.selectAll();
        } else {
            list = sysPermService.findPermsByUserId(user.getUserId());
        }
        List<Map<String, Object>> retLists = new ArrayList<>();
        for (SysPerm sysPerm : list) {
            String group = sysPerm.getPermGroup();
            String permOperation = sysPerm.getPermOperation();

            boolean exist = retLists.stream().filter(e -> StringUtils.equals(group, (String) e.get("id"))).count() > 0;
            Map<String, Object> rts;
            if (!exist) {
                rts = new HashMap<>();
                retLists.add(rts);
                rts.put("id", group);
                rts.put("operation", new HashSet<>());
            } else {
                rts = retLists.stream().filter(e -> StringUtils.equals(group, (String) e.get("id"))).collect(Collectors.toList()).get(0);
            }
            if (StringUtil.isNotEmpty(permOperation)) {
                Set ee = (Set) rts.get("operation");
                ee.add(permOperation);
            }
        }
        return retLists;
    }

//    @RequestMapping("/error")
//    public String error(Model model){
//        throw BusinessException.build("系统异常");
//    }

}