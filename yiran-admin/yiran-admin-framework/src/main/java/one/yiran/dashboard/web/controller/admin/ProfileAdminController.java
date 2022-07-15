package one.yiran.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.annotation.RequireUserLogin;
import one.yiran.dashboard.common.constants.BusinessType;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.util.FileUploadUtil;
import one.yiran.dashboard.common.util.UserCacheUtil;
import one.yiran.dashboard.manage.entity.SysUser;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import one.yiran.dashboard.manage.security.service.PasswordService;
import one.yiran.dashboard.manage.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@AjaxWrapper
@RequestMapping("/system/user/profile")
public class ProfileAdminController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordService passwordService;

    @RequireUserLogin
    @GetMapping("/checkPassword")
    public boolean checkPassword(String password) {
        UserSession user = UserInfoContextHelper.getLoginUser();
        SysUser dbUser = sysUserService.findUser(user.getUserId());

        if (passwordService.matches(dbUser, password)) {
            return true;
        }
        return false;
    }

    @Log(title = "修改密码", businessType = BusinessType.EDIT)
    @PostMapping("/modifyPwd")
    @RequireUserLogin
    public void modifyPwd(@ApiParam(required = true) String oldPassword,@ApiParam(required = true) String newPassword) {
        UserSession loginUser = UserInfoContextHelper.getLoginUser();
        SysUser user = sysUserService.findUser(loginUser.getUserId());
        if (StringUtils.isEmpty(newPassword)) {
            throw BusinessException.build("修改密码失败，新密码不能为空");
        }

        if (passwordService.matches(user, oldPassword)) {
            String newPass = passwordService.encryptPassword(newPassword, Global.getSalt());
            sysUserService.resetUserPwd(user.getUserId(), newPass,Global.getSalt());
        } else {
            throw BusinessException.build("修改密码失败，旧密码输入错误");
        }
    }

    @Log(title = "重置支付密码", businessType = BusinessType.EDIT)
    @PostMapping("/resetAssertPwd")
    @RequireUserLogin
    public void resetAssertPwd(@ApiParam(required = true) String token,
                               @ApiParam(required = true) String code,
                               @ApiParam(required = true) String password) {
        UserSession loginUser = UserInfoContextHelper.getLoginUser();
        SysUser user = sysUserService.findUser(loginUser.getUserId());
        if (StringUtils.isEmpty(password)) {
            throw BusinessException.build("修改密码失败，新支付密码不能为空");
        }
        verifyPhoneCode(token, user.getPhoneNumber(), code);

//        if (passwordService.assertMatches(user, newAssertPassword)) {
            String newPass = passwordService.encryptPassword(password, Global.getSalt());
            sysUserService.resetUserAssetPwd(user.getUserId(), newPass,Global.getSalt());
//        } else {
//            throw BusinessException.build("修改密码失败，旧支付密码输入错误");
//        }
    }

    private void verifyPhoneCode(String token, String phone, String code) {
        if (StringUtils.isBlank(phone)) {
            throw BusinessException.build("手机号不能为空");
        }
        if (StringUtils.isBlank(token)) {
            log.info("token不能为空");
            throw BusinessException.build("请获取验证码");
        }
        if (!token.endsWith(phone)) {
            log.info("非法请求，用户换手机号了，要用发验证码的手机号");
            throw BusinessException.build("非法请求，要用发验证码的手机号");
        }
        String cacheCode = UserCacheUtil.getSmsInfo(token);
        if (cacheCode == null) {
            throw BusinessException.build("短信验证码过期");
        }
        if (!StringUtils.equals(cacheCode, code)) {
            throw BusinessException.build("短信验证码不正确");
        }
    }

    /**
     * 修改用户
     */
    @RequireUserLogin
    @Log(title = "个人信息", businessType = BusinessType.EDIT)
    @PostMapping("/update")
    public void update(String userName, String phoneNumber, String email, String sex) {
        UserSession loginUser = UserInfoContextHelper.getLoginUser();
        sysUserService.updateMyInfos(loginUser.getUserId(),userName,email,phoneNumber,sex);
    }

    /**
     * 保存头像
     */
    @RequireUserLogin
    @Log(title = "个人头像", businessType = BusinessType.EDIT)
    @PostMapping("/updateAvatar")
    public void updateAvatar(@RequestParam("avatarfile") MultipartFile file) {
        UserSession loginUser = UserInfoContextHelper.getLoginUser();
        try {
            if (!file.isEmpty()) {
                String avatar = FileUploadUtil.upload(Global.getAvatarPath(), file);
                sysUserService.updateMyAvatar(loginUser.getUserId(),avatar);
            } else {
                throw BusinessException.build("头像不能为空");
            }
        } catch (Exception e) {
            log.error("修改头像失败！", e);
            throw BusinessException.build(e.getMessage());
        }
    }
}
