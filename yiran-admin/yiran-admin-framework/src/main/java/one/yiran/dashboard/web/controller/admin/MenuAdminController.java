package one.yiran.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.domain.PageRequest;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiObject;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.entity.SysMenu;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysMenuService;
import one.yiran.db.common.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/system/menu")
@AjaxWrapper
public class MenuAdminController {

    @Autowired
    private SysMenuService sysMenuService;

    @RequirePermission(PermissionConstants.Menu.VIEW)
    @PostMapping("/list")
    public List<SysMenu> list(@RequestBody SysMenu sysMenu, HttpServletRequest request) {
        PageRequest rq = PageRequestUtil.fromRequest(request);
        List<SysMenu> sysMenuList = sysMenuService.selectMenuList(rq, sysMenu);
        return sysMenuList;
    }

    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @RequirePermission(PermissionConstants.Menu.REMOVE)
    @RequestMapping("/remove")
    public long remove(@ApiParam Long[] menuIds) {
        return sysMenuService.deleteByPIds(menuIds);
    }

    /**
     * 新增保存菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.ADD)
    @RequirePermission(PermissionConstants.Menu.ADD)
    @PostMapping("/add")
    public int addSave(@ApiObject(validate = true) SysMenu sysMenu) {
        if (!sysMenuService.checkMenuNameUnique(sysMenu)) {
            throw BusinessException.build("新增菜单'" + sysMenu.getMenuName() + "'失败，菜单名称已存在");
        }
        return sysMenuService.insertMenu(sysMenu);
    }

    /**
     * 修改保存菜单
     */
    @Log(title = "菜单管理", businessType = BusinessType.EDIT)
    @RequirePermission(PermissionConstants.Menu.EDIT)
    @PostMapping("/edit")
    public int editSave(@ApiObject(validate = true) SysMenu sysMenu) {
        if (!sysMenuService.checkMenuNameUnique(sysMenu)) {
            throw BusinessException.build("修改菜单'" + sysMenu.getMenuName() + "'失败，菜单名称已存在");
        }
        return sysMenuService.updateMenu(sysMenu);
    }

    /**
     * 校验菜单名称
     */
    @PostMapping("/checkMenuNameUnique")
    public boolean checkMenuNameUnique(SysMenu sysMenu) {
        return sysMenuService.checkMenuNameUnique(sysMenu);
    }

}
