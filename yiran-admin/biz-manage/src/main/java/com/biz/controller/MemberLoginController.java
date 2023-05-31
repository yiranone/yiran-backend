package com.biz.controller;

import com.biz.entity.Member;
import com.biz.service.MemberService;
import com.biz.service.util.MemberPasswordService;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.util.MemberCacheUtil;
import com.biz.vo.MemberVO;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiChannel;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.RequireMemberLogin;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.common.constants.UserConstants;
import one.yiran.dashboard.common.expection.user.UserBlockedException;
import one.yiran.dashboard.common.expection.user.UserDeleteException;
import one.yiran.dashboard.common.util.MessageUtil;
import one.yiran.dashboard.entity.SysChannel;
import one.yiran.dashboard.factory.AsyncFactory;
import one.yiran.dashboard.factory.AsyncManager;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.service.SysChannelService;
import one.yiran.dashboard.vo.ChannelVO;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@AjaxWrapper
@Controller
@RequestMapping("/ext/member")
public class MemberLoginController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private SysChannelService channelService;
    @Autowired
    private MemberPasswordService memberPasswordService;

    @RequireMemberLogin
    @RequestMapping("/current")
    public MemberSession current(@ApiChannel ChannelVO channelVO, HttpServletRequest request){
        return MemberCacheUtil.getSessionInfo(request);
    }

    @PostMapping("/login")
    public MemberSession loginByPhone(@ApiChannel ChannelVO channelVO,
                                      @ApiParam(required = true) String phone,
                                      @ApiParam(required = true) String password) {
        Member m = memberService.selectByPhone(channelVO.getChannelId(),phone);
        if(m == null) {
            throw BusinessException.build("用户不存在");
        }
        SysChannel channel = channelService.selectByPId(channelVO.getChannelId());
        MemberVO memberVO = MemberVO.from(m,channel);

        String username = m.getPhone();
        if (Boolean.TRUE.equals(m.getIsDelete())) {
            AsyncManager.me().execute(AsyncFactory.recordMemberLoginInfo(m.getChannelId(),m.getMemberId(),username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.delete")));
            throw new UserDeleteException();
        }

        if (m.getIsDelete() != null && m.getIsDelete().booleanValue()) {
            AsyncManager.me().execute(AsyncFactory.recordMemberLoginInfo(m.getChannelId(),m.getMemberId(),username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.blocked", m.getName())));
            throw new UserBlockedException();
        }

        memberPasswordService.validate(m, password);

        AsyncManager.me().execute(AsyncFactory.recordMemberLoginInfo(m.getChannelId(),m.getMemberId(),"会员" + m.getMemberId() + " "+ m.getPhone(), SystemConstants.LOGIN_SUCCESS, MessageUtil.message("user.login.success")));
        m.setLoginIp(SessionContextHelper.getIp());
        m.setLoginDate(new Date());
        memberService.update(m);

        String randomKey = RandomStringUtils.randomAlphanumeric(38);

        MemberSession memberSession = new MemberSession();
        memberSession.setMemberId(m.getMemberId());
        memberSession.setPhone(m.getPhone());
        memberSession.setName(m.getName());
        memberSession.setChannelCode(channel.getChannelCode());
        memberSession.setChannelName(channel.getChannelName());

        MemberCacheUtil.setSessionInfo(randomKey, memberSession);
        memberSession.setToken(randomKey);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, Global.getSessionTimeout().intValue());
        memberSession.setTokenExpires(c.getTimeInMillis());

        return memberSession;
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) {
        MemberSession memberSession = MemberCacheUtil.getSessionInfo(request);
        if (memberSession != null) {
            String phone = memberSession.getPhone();
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordMemberLoginInfo(memberSession.getChannelId(),memberSession.getMemberId(),phone, SystemConstants.LOGOUT, MessageUtil.message("user.logout.success")));
            //设置数据库里面的用户状态为离线
            log.info("设置用户{}为离线状态 token={}",phone, memberSession.getToken());
            // 清理缓存
            MemberCacheUtil.removeSessionInfo(memberSession.getToken());
            return "会员退出登陆成功";
        }
        return "会员退出登陆异常";
    }
}
