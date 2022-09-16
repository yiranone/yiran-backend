package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.*;
import one.yiran.dashboard.entity.SysOperateLog;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysOperLogService;
import one.yiran.dashboard.common.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@AjaxWrapper
@Controller
@RequestMapping("/system/operate/log")
public class SysOperateLogController {

    @Autowired
    private SysOperLogService sysOperLogService;

    @RequirePermission(PermissionConstants.OperateLog.VIEW)
    @PostMapping("/list")
    public PageModel list(@ApiObject(createIfNull = true) SysOperateLog sysOperateLog, HttpServletRequest request) {
        return sysOperLogService.selectPage(PageRequestUtil.fromRequest(request), sysOperateLog);
    }

    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.OperateLog.EXPORT)
    @PostMapping("/export")
    public void export(@ApiObject(createIfNull = true) SysOperateLog sysOperateLog, HttpServletRequest request, HttpServletResponse response) {
        List<SysOperateLog> list = sysOperLogService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysOperateLog);
        ExcelUtil<SysOperateLog> util = new ExcelUtil<>(SysOperateLog.class);
        util.exportExcel(response, list, "操作日志");
    }

    @RequirePermission(PermissionConstants.OperateLog.REMOVE)
    @PostMapping("/remove")
    public long delete(@ApiParam(required = true) Long[] operateIds) {
        return sysOperLogService.removeByPIds(operateIds);
    }

    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @RequirePermission(PermissionConstants.OperateLog.REMOVE)
    @PostMapping("/clean")
    public void clean() {
        sysOperLogService.removeAll();
    }

}
