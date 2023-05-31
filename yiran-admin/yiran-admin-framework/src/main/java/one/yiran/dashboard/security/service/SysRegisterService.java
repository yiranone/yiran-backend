package one.yiran.dashboard.security.service;

import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.common.constants.UserConstants;
import one.yiran.dashboard.common.util.MessageUtil;
import one.yiran.dashboard.entity.SysUser;
import one.yiran.dashboard.factory.AsyncFactory;
import one.yiran.dashboard.factory.AsyncManager;
import one.yiran.dashboard.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SysRegisterService {
    @Autowired
    private SysUserService userService;

    @Autowired
    private PasswordService passwordService;

    /**
     * 注册
     */
    public String register(SysUser user) {
        String msg = "", username = user.getLoginName(), password = user.getPassword();

        SysUser regUser = new SysUser();
        regUser.setLoginName(username);
        if (StringUtils.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "用户密码不能为空";
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        } else if (userService.isLoginNameExist(regUser.getLoginName(),regUser.getUserId())) {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        } else {
            user.setSalt(Global.getSalt());
            user.setPassword(passwordService.encryptPassword(user.getPassword(), user.getSalt()));
            SysUser regFlag = userService.registerUser(user);
            if (regFlag == null) {
                msg = "注册失败,请联系系统管理人员";
            } else {
                AsyncManager.me().execute(AsyncFactory.recordUserLoginInfo(user.getChannelId(),user.getUserId(),username, SystemConstants.REGISTER, MessageUtil.message("user.register.success")));
            }
        }
        return msg;
    }
}
