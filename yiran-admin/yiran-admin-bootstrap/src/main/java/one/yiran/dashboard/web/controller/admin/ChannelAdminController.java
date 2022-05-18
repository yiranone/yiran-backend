package one.yiran.dashboard.web.controller.admin;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import one.yiran.common.domain.PageModel;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.manage.entity.SysChannel;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.manage.service.SysChannelService;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.db.common.util.PredicateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/system/channel")
public class ChannelAdminController {

    @Autowired
    private SysChannelService sysChannelService;

    @RequirePermission(PermissionConstants.Channel.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel list(@ApiParam String searchChannelName, @ApiParam String searchChannelCode,
                          @ApiParam String searchStatus,
                          @ApiParam LocalDate searchExpireDate1,@ApiParam LocalDate searchExpireDate2,
                          HttpServletRequest request) {
        SysChannel channel = new SysChannel();
        channel.setIsDelete(false);
        channel.setChannelName(searchChannelName);
        channel.setChannelCode(searchChannelCode);
        channel.setStatus(searchStatus);
        List<Predicate> predicates = new ArrayList<>();
        if(searchExpireDate1 != null)
            predicates.add(PredicateUtil.buildPredicate(Ops.GOE, "expireDate", searchExpireDate1));
        if(searchExpireDate2 != null)
            predicates.add(PredicateUtil.buildPredicate(Ops.LOE, "expireDate", searchExpireDate2));
        return sysChannelService.selectPage(PageRequestUtil.fromRequest(request), channel,predicates);
    }

    @Log(title = PermissionConstants.Channel.NAME, businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Channel.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(SysChannel SysChannel, HttpServletRequest request) {
        PageModel<SysChannel> list = sysChannelService.selectPage(PageRequestUtil.fromRequestIgnorePageSize(request), SysChannel);
        ExcelUtil<SysChannel> util = new ExcelUtil<SysChannel>(SysChannel.class);
        return util.exportExcel(list.getRows(), "参数数据");
    }

    @RequirePermission(PermissionConstants.Channel.ADD)
    @Log(title = PermissionConstants.Channel.NAME, businessType = BusinessType.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public SysChannel addSave(@Validated @RequestBody SysChannel channel) {
        if (!sysChannelService.checkChannelKeyUnique(channel)) {
            throw BusinessException.build("新增渠道'" + channel.getChannelName() + "'失败，渠道代码已存在");
        }
        String loginName = UserInfoContextHelper.getCurrentLoginName();
        channel.setCreateBy(loginName);
        channel.setUpdateBy(loginName);
        return sysChannelService.insert(channel);
    }

    @RequirePermission(PermissionConstants.Channel.EDIT)
    @Log(title = PermissionConstants.Channel.NAME, businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @AjaxWrapper
    public SysChannel editSave(@Validated @RequestBody SysChannel channel) {
        if (!sysChannelService.checkChannelKeyUnique(channel)) {
            throw BusinessException.build("修改参数'" + channel.getChannelCode() + "'失败，渠道代码已存在");
        }
        SysChannel dbChannel = sysChannelService.selectByPId(channel.getChannelId());
        dbChannel.setChannelName(channel.getChannelName());
        dbChannel.setChannelCode(channel.getChannelCode());
        dbChannel.setStatus(channel.getStatus());
        dbChannel.setExpireDate(channel.getExpireDate());
        String loginName = UserInfoContextHelper.getCurrentLoginName();
        dbChannel.setUpdateBy(loginName);
        return sysChannelService.update(dbChannel);
    }

    @RequirePermission(PermissionConstants.Channel.REMOVE)
    @Log(title = PermissionConstants.Channel.NAME, businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(@ApiParam(required = true) Long[] ids) {
        return sysChannelService.deleteByPIds(ids);
    }

    @PostMapping("/checkChannelKeyUnique")
    @ResponseBody
    public boolean checkChannelKeyUnique(@RequestBody SysChannel SysChannel) {
        return sysChannelService.checkChannelKeyUnique(SysChannel);
    }
}