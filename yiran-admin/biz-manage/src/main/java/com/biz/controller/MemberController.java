package com.biz.controller;

import com.biz.entity.Member;
import com.biz.service.MemberService;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiChannel;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.manage.security.config.PermissionConstants;
import one.yiran.dashboard.vo.ChannelVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@AjaxWrapper
@Controller
@RequestMapping("/ext/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @RequirePermission(PermissionConstants.Config.VIEW)
    @PostMapping("/login")
    public Member loginByPhone(@ApiChannel ChannelVO channelVO,
                               @ApiParam(required = true) String phone) {
        Member m = memberService.selectByPhone(channelVO.getChannelId(),phone);
        if(m == null) {
            throw BusinessException.build("用户不存在");
        }
        return m;
    }

}
