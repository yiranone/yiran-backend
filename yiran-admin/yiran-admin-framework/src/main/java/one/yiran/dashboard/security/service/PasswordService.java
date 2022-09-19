package one.yiran.dashboard.security.service;

import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.common.expection.user.UserBlockedException;
import one.yiran.dashboard.common.expection.user.UserPasswordNotMatchException;
import one.yiran.dashboard.common.util.MD5Util;
import one.yiran.dashboard.common.util.MessageUtil;
import one.yiran.dashboard.entity.SysUser;
import one.yiran.dashboard.factory.AsyncFactory;
import one.yiran.dashboard.factory.AsyncManager;
import one.yiran.dashboard.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PasswordService {

    @Autowired
    private SysUserService sysUserService;

    public void validate(SysUser user, String password) {
        String loginName = user.getLoginName();

        long passwordErrorCount = user.getPasswordErrorCount() == null ? 0 : user.getPasswordErrorCount().longValue();
        Date passwordErrorTime = user.getPasswordErrorTime();
        long passwordLimitCount = Global.getPasswordLimitCount();
        long passwordLimitTime = Global.getPasswordLimitTime();
        if(!timeExpire(passwordErrorTime)) {
            if(passwordErrorCount >= passwordLimitCount) {
                throw new UserBlockedException("用户密码错误次数超过" +passwordLimitCount+ "次，请" + passwordLimitTime + "小时后重试，或者联系管理员解锁",loginName);
            }
        }

        Long loginUserId = user.getUserId();
        if (!matches(user, password)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(loginName, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.retry.limit.count", passwordErrorCount)));
            if(timeExpire(passwordErrorTime)) {
                passwordErrorCount = 1;
            } else {
                passwordErrorCount = passwordErrorCount + 1;
            }
            sysUserService.recordLoginFail(loginUserId, passwordErrorCount);
            throw new UserPasswordNotMatchException("密码不正确",loginName);
        } else {
            sysUserService.resetLoginFail(loginUserId);
        }
    }

    public static boolean timeExpire(Date passwordErrorTime){
        long passwordLimitTime = Global.getPasswordLimitTime();
        if(passwordErrorTime != null && System.currentTimeMillis() - passwordErrorTime.getTime() > passwordLimitTime * 3600 * 1000) {
            return true;
        }
        return false;
    }

    public boolean matches(SysUser user, String rawPassword) {
        return user.getPassword().equals(encryptPassword(rawPassword, user.getSalt()));
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
