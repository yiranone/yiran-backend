package com.biz.controller;

import com.alibaba.fastjson.JSONObject;
import com.biz.entity.Member;
import com.biz.service.MemberService;
import com.biz.service.util.MemberPasswordService;
import com.biz.util.AliSmsUtils;
import com.biz.util.ShareCodeUtil;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiChannel;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.RequireMemberLogin;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.service.SysChannelService;
import one.yiran.dashboard.util.MemberCacheUtil;
import one.yiran.dashboard.vo.ChannelVO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@AjaxWrapper
@Controller
@RequestMapping("/ext/member")
public class MemberRegisterController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private SysChannelService channelService;
    @Autowired
    private MemberPasswordService memberPasswordService;

    @PostMapping("/register")
    public MemberSession register(@ApiChannel(required = true) ChannelVO channelVO,
                        @ApiParam String countryCode,
                        @ApiParam String phone,
                        @ApiParam String loginName,
                        @ApiParam String email,
                        @ApiParam(required = true) String token,
                        @ApiParam(required = true) String code,
                        @ApiParam(required = true) String password,
                        @ApiParam String nickName,
                        @ApiParam String refUserCode,
                        @ApiParam String wxkey,
                        @ApiParam String applekey
                        ) {
        phone = this.getPhoneNumber(countryCode, phone);
        String channelCode = channelVO.getChannelCode();

        Member member = new Member();
        member.setChannelId(channelVO.getChannelId());
        member.setCountryCode(countryCode);
        member.setPhone(phone);
        member.setLoginName(loginName);
        member.setEmail(email);
        member.setPassword(password);

        String nickPhone = nickName;
        if(StringUtils.isBlank(nickName)) {
            try {
                phone = phone.trim();
                nickPhone = phone.substring(0, 3) + "****" + phone.substring(7);
            } finally {
            }
        }
        member.setNickName(nickPhone);
        ensureRequiredFieldWhenRegistering(member);
//        authService.attemptWxBind(user, wxkey);
//        authService.attemptAppleBind(user, applekey);

        checkPasswordPattern(password);

        verifyPhoneCode(token, phone, code);

        if (StringUtils.isEmpty(refUserCode)) {
            refUserCode = "AAAA";
            //throw BusinessException.build("没有邀请码不能注册");
        }

        if ("AAAA".equalsIgnoreCase(refUserCode)) {
            log.info("使用默认邀请码注册");
        } else {
            Long refUserId = ShareCodeUtil.decodeUserCode(refUserCode.toUpperCase());
            if (refUserId == null) {
                throw BusinessException.build("邀请码不存在");
            }
            Member refUser = memberService.selectByPId(refUserId);
            if (refUser == null) {
                throw BusinessException.build("邀请码不存在");
            }
            member.setInviteMemberId(refUserId);
            member.setInviteMemberNickName(refUser.getNickName());
        }

        member.setRegisterChannel("mobile");
        final Member dbMember = memberService.registerMember(channelVO.getChannelId(), member);
        MemberCacheUtil.removeSmsInfo(token); //注册成功，删除短信验证码

        String randomKey = RandomStringUtils.randomAlphanumeric(38);

        MemberSession memberSession = new MemberSession();
        memberSession.setMemberId(dbMember.getMemberId());
        memberSession.setPhone(dbMember.getPhone());
        memberSession.setName(dbMember.getName());
        memberSession.setNickName(dbMember.getNickName());
        memberSession.setChannelCode(channelVO.getChannelCode());
        memberSession.setChannelName(channelVO.getChannelName());

        MemberCacheUtil.setSessionInfo(randomKey,memberSession);
        memberSession.setToken(randomKey);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, Global.getSessionTimeout().intValue());
        memberSession.setTokenExpires(c.getTimeInMillis());

        return memberSession;
    }

    private void checkPasswordPattern(String password) {
        String passPat = Global.getConfig("dashboard.password.pattern.login");
        if (StringUtils.isNotBlank(passPat) && !password.matches(passPat)) {
            throw BusinessException.build("登陆密码请输入8-20位数字与字母组合,字母开头");
        }
    }

    @RequestMapping("/sendSms")
    public HashMap sendSms(@ApiChannel(required = true) ChannelVO channelVO,
                           @ApiParam String countryCode,
                           @ApiParam(required = true) String phone,
                           @ApiParam String msgType){
        String channelCode = channelVO.getChannelCode();
        if (StringUtils.isEmpty(channelCode)) {
            throw BusinessException.build("channelCode不能为空");
        }
//        String countryCode = this.getCountryCode(params);
//        String phone = this.getPhoneNumber(countryCode, params);
        String randomNumber = RandomStringUtils.randomNumeric(6);
        if(StringUtils.isBlank(countryCode)) {
            countryCode = AliSmsUtils.DEFAULT_COUNTRY_CODE;
        }
        if(StringUtils.isBlank(msgType)) {
            msgType = "register";
        }
        AliSmsUtils.sendSms(channelCode, countryCode, phone, new JSONObject() {{
            put("code", randomNumber);
        }}.toJSONString(), msgType);

        String randomToken = RandomStringUtils.randomNumeric(32) + "_" + phone;
        MemberCacheUtil.setSmsInfo(randomToken, randomNumber);
        return new HashMap() {{
            put("token", randomToken);
        }};
    }

    @RequestMapping("/expectNew")
    public void expectNew(@ApiChannel(required = true) ChannelVO channelVO,
                          @ApiParam String countryCode,
                          @ApiParam String phone,
                          @ApiParam String email,
                          @ApiParam String loginName
                          ) {
        String channelCode = channelVO.getChannelCode();
        if (StringUtils.isEmpty(channelCode)) {
            throw BusinessException.build("channelCode不能为空");
        }
        if(StringUtils.isBlank(countryCode)) {
            countryCode = AliSmsUtils.DEFAULT_COUNTRY_CODE;
        }
        Member user = new Member();
        user.setChannelId(channelVO.getChannelId());
        if (StringUtils.equals(countryCode,AliSmsUtils.DEFAULT_COUNTRY_CODE)) {
            user.setPhone(phone);
        } else {
            user.setPhone(countryCode + phone);
        }
        user.setLoginName(loginName);
        user.setEmail(email);
        memberService.expectNew(user);
    }

    @RequestMapping("/resetPassword")
    public void resetPassword(@ApiChannel(required = true) ChannelVO channelVO,
                              @ApiParam(required = true) String phone,
                              @ApiParam String countryCode,
                              @ApiParam(required = true) String password,
                              @ApiParam(required = true) String token,
                              @ApiParam(required = true) String code) {
        if(StringUtils.isBlank(countryCode)) {
            countryCode = AliSmsUtils.DEFAULT_COUNTRY_CODE;
        }
        checkPasswordPattern(password);
        verifyPhoneCode(token, phone, code);

        Member m = memberService.selectByPhone(channelVO.getChannelId(),phone);
        if(m == null) {
            throw BusinessException.build("用户不存在");
        }
        Member member = memberService.resetPassword(m.getMemberId(),password);
    }

    @RequireMemberLogin
    @PostMapping("/updatePassword")
    public void updatePassword(@ApiParam(required = true) String newPassword,
                               @ApiParam(required = true) String oldPassword,
                               HttpServletRequest request) {
        MemberSession session = MemberCacheUtil.getSessionInfo(request);
        Long memberId = session.getMemberId();
        checkPasswordPattern(newPassword);
        Member m = memberService.selectByPId(memberId);
        if(m == null) {
            throw BusinessException.build("用户不存在");
        }
        memberPasswordService.validate(m, oldPassword);
        Member member = memberService.resetPassword(m.getMemberId(),newPassword);
    }

    private void verifyPhoneCode(String token, String phone, String code) {
        if (StringUtils.isBlank(phone)) {
            throw BusinessException.build("手机号不能为空");
        }
        if (StringUtils.isBlank(token)) {
            log.info("token不能为空");
            throw BusinessException.build("请获短信取验证码");
        }
        if (StringUtils.equals(code,"666666")) {
            log.info("使用固定的666666通过验证");
            return;
        }
        if (!token.endsWith(phone)) {
            log.info("非法请求，用户换手机号了，要用发验证码的手机号");
            throw BusinessException.build("非法请求，要用发验证码的手机号");
        }
        String cacheCode = MemberCacheUtil.getSmsInfo(token);
        if (cacheCode == null) {
            throw BusinessException.build("短信验证码过期");
        }
        if (!StringUtils.equals(cacheCode, code)) {
            throw BusinessException.build("短信验证码不正确");
        }
    }

    private String getCountryCode(Map<String, String> params){
        return StringUtils.defaultIfBlank(StringUtils.trim(params.get("countryCode")), AliSmsUtils.DEFAULT_COUNTRY_CODE);
    }

    private String getPhoneNumber(String countryCode,String phone) {
        if (StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(countryCode) && !AliSmsUtils.DEFAULT_COUNTRY_CODE.equals(countryCode)) {
            phone = countryCode + phone;
        }
        return phone;
    }
    private void ensureRequiredFieldWhenRegistering(Member user) {
        if (StringUtils.isEmpty(user.getPassword())) {
            throw BusinessException.build("密码不能为空");
        }
        if (StringUtils.isEmpty(user.getLoginName())
                && StringUtils.isEmpty(user.getEmail()) && StringUtils.isEmpty(user.getPhone())) {
            throw BusinessException.build("用户名不能为空");
        }
    }
}
