package one.yiran.dashboard.web.controller.admin;

import com.querydsl.core.types.Predicate;
import one.yiran.common.domain.PageModel;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiObject;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.util.ExcelUtil;
import one.yiran.dashboard.entity.QSysChannel;
import one.yiran.dashboard.entity.SysChannel;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.security.config.PermissionConstants;
import one.yiran.dashboard.service.SysChannelService;
import one.yiran.db.common.util.PageRequestUtil;
import one.yiran.db.common.util.PredicateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@Controller
@AjaxWrapper
@RequestMapping("/system/channel")
public class ChannelAdminController {

    @Autowired
    private SysChannelService sysChannelService;

    @RequirePermission(PermissionConstants.Channel.VIEW)
    @PostMapping("/detail")
    public SysChannel detail(@ApiParam(required = true) Long channelId) {
        return sysChannelService.selectByPId(channelId);
    }

    @RequirePermission(PermissionConstants.Channel.VIEW)
    @PostMapping("/list")
    public PageModel list(@ApiParam String searchChannelName, @ApiParam String searchChannelCode,
                          @ApiParam String searchStatus,
                          @ApiParam LocalDate searchExpireDate1,@ApiParam LocalDate searchExpireDate2,
                          HttpServletRequest request) {
        SysChannel channel = new SysChannel();
        channel.setIsDelete(false);
        channel.setChannelName(searchChannelName);
        channel.setChannelCode(searchChannelCode);
        channel.setStatus(searchStatus);

        List<Predicate> ps = PredicateBuilder.builder()
                .addGreaterOrEqualIfNotBlank(QSysChannel.sysChannel.expireDate, searchExpireDate1)
                .addLittlerOrEqualIfNotBlank(QSysChannel.sysChannel.expireDate, searchExpireDate2)
                .toList();

        return sysChannelService.selectPage(PageRequestUtil.fromRequest(request), channel,ps);
    }

    @Log(title = PermissionConstants.Channel.NAME, businessType = BusinessType.EXPORT)
    @RequirePermission(PermissionConstants.Channel.EXPORT)
    @PostMapping("/export")
    public String export(SysChannel SysChannel, HttpServletRequest request) {
        PageModel<SysChannel> list = sysChannelService.selectPage(PageRequestUtil.fromRequestIgnorePageSize(request), SysChannel);
        ExcelUtil<SysChannel> util = new ExcelUtil<SysChannel>(SysChannel.class);
        return util.exportExcel(list.getRows(), "参数数据");
    }

    @RequirePermission(PermissionConstants.Channel.ADD)
    @Log(title = PermissionConstants.Channel.NAME, businessType = BusinessType.ADD)
    @PostMapping("/add")
    public SysChannel addSave(@ApiObject(validate = true) SysChannel channel) {
        if (!sysChannelService.checkChannelKeyUnique(channel)) {
            throw BusinessException.build("新增渠道'" + channel.getChannelName() + "'失败，渠道代码已存在");
        }
        String loginName = SessionContextHelper.getCurrentLoginName();
        channel.setCreateBy(loginName);
        channel.setUpdateBy(loginName);
        return sysChannelService.create(channel);
    }

    @RequirePermission(PermissionConstants.Channel.EDIT)
    @Log(title = PermissionConstants.Channel.NAME, businessType = BusinessType.EDIT)
    @PostMapping("/edit")
    public SysChannel editSave(@ApiObject(validate = true) SysChannel channel) {
        if (!sysChannelService.checkChannelKeyUnique(channel)) {
            throw BusinessException.build("修改参数'" + channel.getChannelCode() + "'失败，渠道代码已存在");
        }
        SysChannel dbChannel = sysChannelService.selectByPId(channel.getChannelId());
        dbChannel.setChannelName(channel.getChannelName());
        dbChannel.setChannelCode(channel.getChannelCode());
        dbChannel.setChannelSort(channel.getChannelSort());
        dbChannel.setStatus(channel.getStatus());
        dbChannel.setExpireDate(channel.getExpireDate());
        String loginName = SessionContextHelper.getCurrentLoginName();
        dbChannel.setUpdateBy(loginName);
        return sysChannelService.update(dbChannel);
    }

    @RequirePermission(PermissionConstants.Channel.DELETE)
    @Log(title = PermissionConstants.Channel.NAME, businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    public long remove(@ApiParam(required = true) Long[] channelIds) {
        return sysChannelService.deleteByPIds(channelIds);
    }

    @PostMapping("/checkChannelKeyUnique")
    public boolean checkChannelKeyUnique(@ApiObject SysChannel SysChannel) {
        return sysChannelService.checkChannelKeyUnique(SysChannel);
    }
}