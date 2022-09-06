package one.yiran.dashboard.web.controller;

import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.constants.ShiroConstants;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.common.constants.UserConstants;
import one.yiran.dashboard.common.expection.CaptchaException;
import one.yiran.dashboard.common.util.*;
import one.yiran.dashboard.manage.entity.SysUser;
import one.yiran.dashboard.manage.entity.SysUserOnline;
import one.yiran.dashboard.manage.factory.AsyncFactory;
import one.yiran.dashboard.manage.factory.AsyncManager;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.security.service.PasswordService;
import one.yiran.dashboard.common.expection.user.UserBlockedException;
import one.yiran.dashboard.common.expection.user.UserDeleteException;
import one.yiran.dashboard.common.expection.user.UserNotFoundException;
import one.yiran.dashboard.common.expection.user.UserPasswordNotMatchException;
import one.yiran.dashboard.manage.service.SysMenuService;
import one.yiran.dashboard.manage.service.SysRoleService;
import one.yiran.dashboard.manage.service.SysUserOnlineService;
import one.yiran.dashboard.manage.service.SysUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 * 登录验证
 */
@Controller
@Slf4j
public class UserLoginController {

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysUserOnlineService onlineService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    @AjaxWrapper
    public UserSession ajaxLogin(@ApiParam String username, @ApiParam String password) {
        if(Global.isDebugMode()) {
            String debugUserName = Global.getDebugLoginName();
            String debugPassword = Global.getDebugPassword();
            if(!org.apache.commons.lang3.StringUtils.equals(debugUserName,username)) {
                throw BusinessException.build("维护中，"+username+"不能登陆");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(debugPassword,password)) {
                throw BusinessException.build("维护中，"+username+"密码不正确，不能登陆");
            }
            SysUser user = sysUserService.findUserByLoginName(username);
            if (user == null) {
                throw BusinessException.build("虚拟" + username + "不存在");
            }
            log.info("DebugMode用户{}登陆成功",username);
            String randomKey = RandomStringUtils.randomAscii(32);
            UserSession ui = UserConvertUtil.convert(user);
            UserCacheUtil.setSessionInfo(randomKey,ui);
            return ui;
        }
        // 验证码校验
        if (!org.springframework.util.StringUtils.isEmpty(ServletUtil.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA))) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户名或密码为空 错误
        if (org.springframework.util.StringUtils.isEmpty(username) || org.springframework.util.StringUtils.isEmpty(password)) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("not.null")));
            throw new UserNotFoundException(username);
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException("用户名长度不正确，长度应该在"+ UserConstants.USERNAME_MIN_LENGTH +"和" + UserConstants.USERNAME_MAX_LENGTH + "之间",username);
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException("密码长度不正确，长度应该在"+ UserConstants.PASSWORD_MIN_LENGTH +"和" + UserConstants.PASSWORD_MAX_LENGTH + "之间",username);
        }
        // 查询用户信息
        SysUser user = sysUserService.findUserByLoginName(username);

        if (user == null && maybeMobilePhoneNumber(username)) {
            user = sysUserService.findUserByPhoneNumber(username);
        }

        if (user == null && maybeEmail(username)) {
            user = sysUserService.findUserByEmail(username);
        }

        if (user == null) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.not.exists")));
            throw new UserNotFoundException(username);
        }

        if (Boolean.TRUE.equals(user.getIsDelete())) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.delete")));
            throw new UserDeleteException();
        }

        if (UserConstants.USER_DELETED.equals(user.getStatus())) {
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.blocked", user.getRemark())));
            throw new UserBlockedException();
        }

        passwordService.validate(user, password);

        AsyncManager.me().execute(AsyncFactory.recordLoginInfo(username, SystemConstants.LOGIN_SUCCESS, MessageUtil.message("user.login.success")));
        String ip = IpUtil.getIpAddr(ServletUtil.getRequest());
        user = sysUserService.recordLoginIp(user.getUserId(), ip);

        String randomKey = RandomStringUtils.randomAlphanumeric(38);
        UserSession ui = UserConvertUtil.convert(user);

        //设置用户的来源系统
        ui.setChannelId(user.getChannelId());
        ui.setToken(randomKey);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND,UserCacheUtil.getSessionTimeout());
        ui.setTokenExpires(c.getTimeInMillis());
        UserCacheUtil.setSessionInfo(randomKey,ui);

        //在线用户
        AsyncManager.me().execute(AsyncFactory.recordOnlineInfo(ui));

        return ui;
    }

    private boolean maybeEmail(String username) {
        if (!username.matches(UserConstants.EMAIL_PATTERN)) {
            return false;
        }
        return true;
    }

    private boolean maybeMobilePhoneNumber(String username) {
        if (!username.matches(UserConstants.MOBILE_PHONE_NUMBER_PATTERN)) {
            return false;
        }
        return true;
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request) {
        UserSession user = UserCacheUtil.getSessionInfo(request);
        if (user != null) {
            String loginName = user.getLoginName();
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLoginInfo(loginName, SystemConstants.LOGOUT, MessageUtil.message("user.logout.success")));
            // 清理缓存
            UserCacheUtil.removeSessionInfo(user.getToken());
        }
    }
}
