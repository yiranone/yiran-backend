package com.biz.extcontroller;

import com.biz.entity.Member;
import com.biz.service.MemberService;
import com.biz.service.u.MemberPasswordService;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.common.util.MemberCacheUtil;
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
import one.yiran.dashboard.common.util.UserCacheUtil;
import one.yiran.dashboard.manage.entity.SysChannel;
import one.yiran.dashboard.manage.factory.AsyncFactory;
import one.yiran.dashboard.manage.factory.AsyncManager;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.service.SysChannelService;
import one.yiran.dashboard.vo.ChannelVO;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

@AjaxWrapper
@Controller
@RequestMapping("/ext/member")
public class ExtMemberController {

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
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.delete")));
            throw new UserDeleteException();
        }

        if (UserConstants.USER_DELETED.equals(m.getStatus())) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.blocked", m.getName())));
            throw new UserBlockedException();
        }

        memberPasswordService.validate(m, password);

        AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_SUCCESS, MessageUtil.message("user.login.success")));
        m.setLoginIp(UserInfoContextHelper.getIp());
        m.setLoginDate(new Date());

        String randomKey = RandomStringUtils.randomAlphanumeric(38);

        MemberSession memberSession = new MemberSession();
        memberSession.setMemberId(m.getMemberId());
        memberSession.setPhone(m.getPhone());
        memberSession.setName(m.getName());
        memberSession.setChannelCode(channel.getChannelCode());
        memberSession.setChannelName(channel.getChannelName());

        MemberCacheUtil.setSessionInfo(randomKey,memberSession);
        memberSession.setToken(randomKey);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND,UserCacheUtil.getSessionTimeout());
        memberSession.setTokenExpires(c.getTimeInMillis());

        return memberSession;
    }
}
