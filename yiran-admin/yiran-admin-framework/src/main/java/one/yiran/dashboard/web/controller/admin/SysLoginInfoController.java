package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.manage.security.service.PasswordService;
import one.yiran.dashboard.manage.entity.SysLoginInfo;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.service.SysLoginInfoService;
import one.yiran.dashboard.common.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@AjaxWrapper
@Controller
@RequestMapping("/system/login/info")
public class SysLoginInfoController {

    @Autowired
    private SysLoginInfoService sysLoginInfoService;

    @RequirePermission(PermissionConstants.LoginInfo.VIEW)
    @PostMapping("/list")
    public PageModel<SysLoginInfo> list(@ApiObject(createIfNull = true) SysLoginInfo sysLoginInfo, HttpServletRequest request) {
        return sysLoginInfoService.selectPage(PageRequestUtil.fromRequest(request), sysLoginInfo);
    }

    @Log(title = "登陆日志", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.LoginInfo.EXPORT)
    @PostMapping("/export")
    public void export(@ApiObject(createIfNull = true) SysLoginInfo sysLoginInfo, HttpServletRequest request, HttpServletResponse response) {
        List<SysLoginInfo> list = sysLoginInfoService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysLoginInfo);
        ExcelUtil<SysLoginInfo> util = new ExcelUtil<>(SysLoginInfo.class);
        util.exportExcel(response, list, "登陆日志");
    }

    @RequirePermission(PermissionConstants.LoginInfo.REMOVE)
    @Log(title = "登陆日志", businessType = BusinessType.DELETE)
    @PostMapping("/delete")
    public long delete(@ApiParam Long[] loginInfoIds) {
        return sysLoginInfoService.deleteByPIds(loginInfoIds);
    }

    @RequirePermission(PermissionConstants.LoginInfo.REMOVE)
    @Log(title = "登陆日志", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    public long clean() {
        return sysLoginInfoService.removeAll();
    }

}
