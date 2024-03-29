package one.yiran.dashboard.web.controller;

import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.captcha.service.impl.DefaultCaptchaService;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.common.constants.UserConstants;
import one.yiran.dashboard.common.expection.CaptchaException;
import one.yiran.dashboard.entity.SysChannel;
import one.yiran.dashboard.entity.SysDept;
import one.yiran.dashboard.entity.SysUser;
import one.yiran.dashboard.factory.AsyncFactory;
import one.yiran.dashboard.factory.AsyncManager;
import one.yiran.dashboard.security.service.PasswordService;
import one.yiran.dashboard.common.expection.user.UserBlockedException;
import one.yiran.dashboard.common.expection.user.UserDeleteException;
import one.yiran.dashboard.common.expection.user.UserNotFoundException;
import one.yiran.dashboard.common.expection.user.UserPasswordNotMatchException;
import one.yiran.dashboard.service.SysChannelService;
import one.yiran.dashboard.service.SysDeptService;
import one.yiran.dashboard.service.SysUserOnlineService;
import one.yiran.dashboard.service.SysUserService;
import one.yiran.dashboard.common.util.IpUtil;
import one.yiran.dashboard.common.util.MessageUtil;
import one.yiran.dashboard.common.util.ServletUtil;
import one.yiran.dashboard.util.UserCacheUtil;
import one.yiran.dashboard.util.UserConvertUtil;
import one.yiran.dashboard.vo.UserPageVO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录验证
 */
@AjaxWrapper
@Controller
@Slf4j
public class UserLoginController {

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private SysUserOnlineService onlineService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private DefaultCaptchaService captchaService;
    @Autowired
    private SysChannelService sysChannelService;

    @PostMapping("/login")
    public UserPageVO ajaxLogin(@ApiParam String username, @ApiParam String password,
                                @ApiParam String captchaVerification) {
        if(Global.isDebugMode()) {
            return debugFunction(username, password);
        }
        if(!StringUtils.equals(Global.getCaptchaType(),"none")) {
            //开启了验证码
            if(StringUtils.isBlank(captchaVerification))
                throw new CaptchaException("没有输入验证码");
            boolean pass = captchaService.verification(captchaVerification);
            if(!pass) {
                AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(null,null, username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.jcaptcha.error")));
                throw new CaptchaException("验证码校验不通过");
            }
        }
        // 用户名或密码为空 错误
        if (org.springframework.util.StringUtils.isEmpty(username) || org.springframework.util.StringUtils.isEmpty(password)) {
            AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(null,null,username, SystemConstants.LOGIN_FAIL, MessageUtil.message("not.null")));
            throw new UserNotFoundException(username);
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(null,null,username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
            throw new UserPasswordNotMatchException("用户名长度不正确，长度应该在"+ UserConstants.USERNAME_MIN_LENGTH +"和" + UserConstants.USERNAME_MAX_LENGTH + "之间",username);
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(null,null,username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.not.match")));
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
            AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(null,null,username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.not.exists")));
            throw new UserNotFoundException(username);
        }

        if (Boolean.TRUE.equals(user.getIsDelete())) {
            AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(user.getChannelId(),user.getUserId(),username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.password.delete")));
            throw new UserDeleteException();
        }

        if (UserConstants.USER_BLOCKED.equals(user.getStatus())) {
            AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(user.getChannelId(),user.getUserId(),username, SystemConstants.LOGIN_FAIL, MessageUtil.message("user.blocked", user.getRemark())));
            throw new UserBlockedException();
        }

        passwordService.validate(user, password);

        AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(user.getChannelId(),user.getUserId(),username, SystemConstants.LOGIN_SUCCESS, MessageUtil.message("user.login.success")));
        String ip = IpUtil.getIpAddr(ServletUtil.getRequest());
        user = sysUserService.recordLoginIp(user.getUserId(), ip);

        String randomKey = RandomStringUtils.randomAlphanumeric(38);
        UserSession us = UserConvertUtil.convert(user);

        //设置用户的来源系统
        us.setChannelId(user.getChannelId());
        us.setToken(randomKey);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, Global.getSessionTimeout().intValue());
        us.setTokenExpires(c.getTimeInMillis());
        UserCacheUtil.setSessionInfo(randomKey,us);

        //在线用户
        AsyncManager.me().execute(AsyncFactory.recordOnlineInfo(us));

        UserPageVO up;
        if(user.getDeptId() != null) {
            SysDept sysDept = sysDeptService.selectByPId(user.getDeptId());
            up = UserPageVO.from(user,sysDept);
        } else {
            up = UserPageVO.from(user);
        }
        up.setToken(us.getToken());
        up.setTokenExpires(us.getTokenExpires());
        return up;
    }

    private UserPageVO debugFunction(String username, String password) {
        String debugUserName = Global.getDebugLoginName();
        String debugPassword = Global.getDebugPassword();
        if(!StringUtils.equals(debugUserName, username)) {
            throw BusinessException.build("维护中，"+ username +"不能登陆");
        }
        if(!StringUtils.equals(debugPassword, password)) {
            throw BusinessException.build("维护中，"+ username +"密码不正确，不能登陆");
        }
        SysUser user = sysUserService.findUserByLoginName(username);
        if (user == null) {
            throw BusinessException.build("虚拟" + username + "不存在");
        }
        log.info("DebugMode用户{}登陆成功", username);
        String randomKey = RandomStringUtils.randomAscii(32);
        UserSession us = UserConvertUtil.convert(user);
        UserCacheUtil.setSessionInfo(randomKey,us);

        UserPageVO up;
        if(user.getDeptId() != null) {
            SysDept sysDept = sysDeptService.selectByPId(user.getDeptId());
            up = UserPageVO.from(user,sysDept);
        } else {
            up = UserPageVO.from(user);
        }
        up.setToken(us.getToken());
        up.setTokenExpires(us.getTokenExpires());
        return up;
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

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) {
        UserSession user = UserCacheUtil.getSessionInfo(request);
        if (user != null) {
            String loginName = user.getLoginName();
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(user.getChannelId(),user.getUserId(),loginName, SystemConstants.LOGOUT, MessageUtil.message("user.logout.success")));
            //设置数据库里面的用户状态为离线
            log.info("设置用户{}为离线状态 token={}",loginName, user.getToken());
            onlineService.forceLogout(user.getToken());
            // 清理缓存
            UserCacheUtil.removeSessionInfo(user.getToken());
            return "退出登陆成功";
        }
        return "退出登陆异常";
    }

    @RequestMapping("/login/config")
    public Map<String,String> loginConfig(HttpServletRequest request) {
        Map<String,String> map = new HashMap<>();
        map.put("captcha",Global.getCaptchaType());

        String hostName = request.getHeader("Host");
        log.info("Host:{}",hostName);
        String logo = null;
        String icon = null;
        String displayName = null;
        if(StringUtils.isNotBlank(hostName)) {
            SysChannel sysChan = sysChannelService.selectByDomainName(hostName);
            if(sysChan != null){
                logo = sysChan.getLogo();
                displayName = sysChan.getDisplayName();
                icon = sysChan.getIcon();
            }
        }
        //map.put("logo","https://theme.zdassets.com/theme_assets/1848125/dd0b0631a75936bfb90a2b1aa61b380738fc9e4c.png");
        map.put("logo",logo);
        map.put("icon",icon);
        map.put("displayName",displayName);
        return map;
    }
}
