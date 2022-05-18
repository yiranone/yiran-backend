package one.yiran.dashboard.web.controller.monitor;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.manage.entity.SysOperLog;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.service.SysOperLogService;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.common.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@AjaxWrapper
@Controller
@RequestMapping("/monitor/operlog")
public class SysOperLogController {

    @Autowired
    private SysOperLogService sysOperLogService;

    @RequirePermission(PermissionConstants.OperLog.VIEW)
    @PostMapping("/list")
    public PageModel list(HttpServletRequest request, SysOperLog sysOperLog) {
        return sysOperLogService.selectPage(PageRequestUtil.fromRequest(request), sysOperLog);
    }

    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.OperLog.EXPORT)
    @PostMapping("/export")
    public String export(HttpServletRequest request, SysOperLog sysOperLog) {
        List<SysOperLog> list = sysOperLogService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysOperLog);
        ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
        return util.exportExcel(list, "操作日志");
    }

    @RequirePermission(PermissionConstants.OperLog.REMOVE)
    @PostMapping("/remove")
    public long remove(@RequestBody Long[] ids) {
        return sysOperLogService.removeByPIds(ids);
    }

    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @RequirePermission(PermissionConstants.OperLog.REMOVE)
    @PostMapping("/clean")
    public void clean() {
        sysOperLogService.removeAll();
    }

}
