package one.yiran.dashboard.web.controller.monitor;

import one.yiran.dashboard.manage.security.service.PasswordService;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.manage.entity.SysLoginInfo;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.service.SysLoginInfoService;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.common.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@AjaxWrapper
@Controller
@RequestMapping("/monitor/logininfor")
public class SysLoginInfoController {

    @Autowired
    private SysLoginInfoService sysLoginInfoService;

    @Autowired
    private PasswordService passwordService;

    @RequirePermission(PermissionConstants.LoginInfo.VIEW)
    @PostMapping("/list")
    public PageModel<SysLoginInfo> list(HttpServletRequest request, SysLoginInfo sysLoginInfo) {
        return sysLoginInfoService.selectPage(PageRequestUtil.fromRequest(request), sysLoginInfo);
    }

    @Log(title = "登陆日志", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.LoginInfo.EXPORT)
    @PostMapping("/export")
    public String export(HttpServletRequest request, SysLoginInfo sysLoginInfo) {
        List<SysLoginInfo> list = sysLoginInfoService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysLoginInfo);
        ExcelUtil<SysLoginInfo> util = new ExcelUtil<SysLoginInfo>(SysLoginInfo.class);
        return util.exportExcel(list, "登陆日志");
    }

    @RequirePermission(PermissionConstants.LoginInfo.REMOVE)
    @Log(title = "登陆日志", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    public long remove(@RequestBody Long[] ids) {
        return sysLoginInfoService.removeByPIds(ids);
    }

    @RequirePermission(PermissionConstants.LoginInfo.REMOVE)
    @Log(title = "登陆日志", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    public long clean() {
        return sysLoginInfoService.removeAll();
    }

}
