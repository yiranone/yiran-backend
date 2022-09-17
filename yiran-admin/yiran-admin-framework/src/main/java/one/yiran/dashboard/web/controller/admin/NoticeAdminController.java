package one.yiran.dashboard.web.controller.admin;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.entity.SysNotice;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysNoticeService;
import one.yiran.dashboard.common.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@AjaxWrapper
@Controller
@RequestMapping("/system/notice")
public class NoticeAdminController {
    private String prefix = "system/notice";

    @Autowired
    private SysNoticeService sysNoticeService;

    @RequirePermission(PermissionConstants.Notice.VIEW)
    @PostMapping("/list")
    public PageModel list(@RequestBody SysNotice sysNotice, HttpServletRequest request) {
        sysNotice.setIsDelete(false);
        return sysNoticeService.selectPage(PageRequestUtil.fromRequest(request), sysNotice);
    }

    @RequirePermission(PermissionConstants.Notice.ADD)
    @Log(title = "通知公告", businessType = BusinessType.ADD)
    @PostMapping("/add")
    public SysNotice addSave(@Validated @RequestBody SysNotice sysNotice) {
        sysNotice.setCreateBy(SessionContextHelper.getCurrentLoginName());
        sysNotice.setUpdateBy(SessionContextHelper.getCurrentLoginName());
        return sysNoticeService.insert(sysNotice);
    }

    @RequirePermission(PermissionConstants.Notice.EDIT)
    @Log(title = "通知公告", businessType = BusinessType.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysNotice editSave(@Validated @RequestBody SysNotice sysNotice) {
        sysNotice.setUpdateBy(SessionContextHelper.getCurrentLoginName());
        return sysNoticeService.update(sysNotice);
    }

    @RequirePermission(PermissionConstants.Notice.REMOVE)
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(@RequestBody Long[] ids) {
        return sysNoticeService.deleteByPIds(ids);
    }
}