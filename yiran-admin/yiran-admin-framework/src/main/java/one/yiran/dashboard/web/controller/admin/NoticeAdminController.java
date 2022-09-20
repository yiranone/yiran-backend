package one.yiran.dashboard.web.controller.admin;

import com.querydsl.core.types.Predicate;
import one.yiran.common.domain.PageModel;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiObject;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.entity.QSysNotice;
import one.yiran.dashboard.entity.SysNotice;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysNoticeService;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.db.common.util.PredicateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@AjaxWrapper
@Controller
@RequestMapping("/system/notice")
public class NoticeAdminController {

    @Autowired
    private SysNoticeService sysNoticeService;

    @PostMapping("/detail")
    @RequirePermission(PermissionConstants.Notice.VIEW)
    public SysNotice detail(@ApiParam(required = true) Long noticeId) {
        return sysNoticeService.selectByPId(noticeId);
    }


    @RequirePermission(PermissionConstants.Notice.VIEW)
    @PostMapping("/list")
    public PageModel list(@ApiObject SysNotice sysNotice,
                          @ApiParam(format = "yyyy-MM-dd") Date createBeginTime,
                          @ApiParam(format = "yyyy-MM-dd") Date createEndTime,
                          HttpServletRequest request) {
        QSysNotice notice = QSysNotice.sysNotice;
        List<Predicate> predicates = PredicateBuilder.builder()
                .addGreaterOrEqualIfNotBlank(notice.createTime, createBeginTime)
                .addLittlerOrEqualIfNotBlank(notice.createTime, createEndTime)
                .addEqualOrNullExpression(notice.isDelete,Boolean.FALSE)
                .toList();
        return sysNoticeService.selectPage(PageRequestUtil.fromRequest(request), sysNotice, predicates);
    }

    @Log(title = "通知公告", businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Notice.EXPORT)
    @PostMapping("/export")
    public void export(@ApiObject(createIfNull = true) SysNotice sysNotice, HttpServletRequest request, HttpServletResponse response) {
        PageModel<SysNotice> list = sysNoticeService.selectPage(PageRequestUtil.fromRequestIgnorePageSize(request), sysNotice);
        ExcelUtil<SysNotice> util = new ExcelUtil<>(SysNotice.class);
        util.exportExcel(response, list.getRows(), "通知公告");
    }

    @RequirePermission(PermissionConstants.Notice.ADD)
    @Log(title = "通知公告", businessType = BusinessType.ADD)
    @PostMapping("/add")
    public SysNotice addSave(@ApiObject SysNotice sysNotice) {
        sysNotice.setCreateBy(SessionContextHelper.getCurrentLoginName());
        sysNotice.setUpdateBy(SessionContextHelper.getCurrentLoginName());
        return sysNoticeService.insert(sysNotice);
    }

    @RequirePermission(PermissionConstants.Notice.EDIT)
    @Log(title = "通知公告", businessType = BusinessType.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysNotice editSave(@ApiObject SysNotice sysNotice) {
        sysNotice.setUpdateBy(SessionContextHelper.getCurrentLoginName());
        return sysNoticeService.update(sysNotice);
    }

    @RequirePermission(PermissionConstants.Notice.DELETE)
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(@ApiParam Long[] noticeIds) {
        return sysNoticeService.deleteByPIds(noticeIds);
    }
}