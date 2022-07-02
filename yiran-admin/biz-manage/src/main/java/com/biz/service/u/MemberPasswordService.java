package com.biz.service.u;

import com.biz.entity.Member;
import com.biz.service.MemberService;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.common.expection.user.UserBlockedException;
import one.yiran.dashboard.common.expection.user.UserPasswordNotMatchException;
import one.yiran.dashboard.common.util.MD5Util;
import one.yiran.dashboard.common.util.MessageUtil;
import one.yiran.dashboard.manage.entity.SysUser;
import one.yiran.dashboard.manage.factory.AsyncFactory;
import one.yiran.dashboard.manage.factory.AsyncManager;
import one.yiran.dashboard.manage.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MemberPasswordService {

    @Autowired
    private MemberService memberService;

    public void validate(Member member, String password) {
        String loginName = member.getPhone();

        long passwordErrorCount = member.getPasswordErrorCount() == null ? 0 : member.getPasswordErrorCount().longValue();
        Date passwordErrorTime = member.getPasswordErrorTime();
        long passwordLimitCount = Global.getPasswordLimitCount();
        long passwordLimitTime = Global.getPasswordLimitTime();
        if(!timeExpire(passwordErrorTime)) {
            if(passwordErrorCount >= passwordLimitCount) {
                throw new UserBlockedException("用户密码错误次数超过" +passwordLimitCount+ "次，请" + passwordLimitTime + "小时后重试，或者联系管理员解锁",loginName);
            }
        }

        Long loginUserId = member.getMemberId();
        if (!matches(member, password)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(loginName, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.retry.limit.count", passwordErrorCount)));
            if(timeExpire(passwordErrorTime)) {
                passwordErrorCount = 1;
            } else {
                passwordErrorCount = passwordErrorCount + 1;
            }
            memberService.recordLoginFail(loginUserId, passwordErrorCount);
            throw new UserPasswordNotMatchException("密码不正确",loginName);
        } else {
            memberService.resetLoginFail(loginUserId);
        }
    }

    public void validateAssert(Member member, String assertRawPassword) {
        String loginName = member.getPhone();
        Long memberId = member.getMemberId();
        if (!assertMatches(member, assertRawPassword)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(loginName, SystemConstants.LOGIN_FAIL, "支付密码错误"));
            throw new UserPasswordNotMatchException("支付密码错误",loginName);
        }
    }

    private boolean timeExpire(Date passwordErrorTime){
        long passwordLimitTime = Global.getPasswordLimitTime();
        if(passwordErrorTime != null && System.currentTimeMillis() - passwordErrorTime.getTime() > passwordLimitTime * 3600 * 1000) {
            return true;
        }
        return false;
    }

    public boolean matches(Member member, String rawPassword) {
        return member.getPassword().equals(encryptPassword(rawPassword, member.getSalt()));
    }

    public boolean assertMatches(Member member, String assertRawPassword) {
        if(member.getAssertSalt() == null)
            return false;
        return member.getAssertPassword().equals(encryptPassword(assertRawPassword, member.getAssertSalt()));
    }

    public String encryptPassword(String password, String salt) {
        if(StringUtils.isBlank(password)) {
            throw BusinessException.build("密码异常，不能为空");
        }
        if(StringUtils.isBlank(salt)) {
            throw BusinessException.build("密码异常，随机因子不能为空");
        }
        return MD5Util.hash(password + salt);
    }
}
