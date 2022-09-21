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
import one.yiran.dashboard.entity.SysDept;
import one.yiran.dashboard.entity.SysDictData;
import one.yiran.dashboard.service.SysDeptService;
import one.yiran.dashboard.util.UserCacheUtil;
import one.yiran.dashboard.entity.SysUser;
import one.yiran.dashboard.security.SessionContextHelper;
import one.yiran.dashboard.security.service.PasswordService;
import one.yiran.dashboard.service.SysUserService;
import one.yiran.dashboard.vo.UserPageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@AjaxWrapper
@RequestMapping("/system/user/profile")
public class ProfileAdminController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private PasswordService passwordService;

    @RequireUserLogin
    @RequestMapping("/my")
    public UserPageVO my() {
        UserSession user = SessionContextHelper.getLoginUser();
        SysUser dbUser = sysUserService.findUser(user.getUserId());
        UserPageVO up;
        if(dbUser.getDeptId() != null) {
            SysDept sysDept = sysDeptService.selectByPId(dbUser.getDeptId());
            up = UserPageVO.from(dbUser,sysDept);
        } else {
            up = UserPageVO.from(dbUser);
        }
        up.setToken(user.getToken());
        up.setTokenExpires(user.getTokenExpires());
        return up;
    }

    @RequireUserLogin
    @RequestMapping("/checkPassword")
    public boolean checkPassword(String password) {
        UserSession user = SessionContextHelper.getLoginUser();
        SysUser dbUser = sysUserService.findUser(user.getUserId());

        if (passwordService.matches(dbUser, password)) {
            return true;
        }
        return false;
    }

    @Log(title = "修改密码", businessType = BusinessType.EDIT)
    @RequestMapping("/modifyPwd")
    @RequireUserLogin
    public void modifyPwd(@ApiParam(required = true) String oldPassword,@ApiParam(required = true) String newPassword) {
        UserSession loginUser = SessionContextHelper.getLoginUser();
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
        UserSession loginUser = SessionContextHelper.getLoginUser();
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
    public void update(@ApiParam String userName,@ApiParam(required = true) String phoneNumber,
                       @ApiParam(required = true) String email,@ApiParam String sex) {
        UserSession loginUser = SessionContextHelper.getLoginUser();
        sysUserService.updateMyInfos(loginUser.getUserId(),userName,email,phoneNumber,sex);
    }

    /**
     * 保存头像
     */
    @RequireUserLogin
    @Log(title = "个人头像", businessType = BusinessType.EDIT)
    @PostMapping("/updateAvatar")
    public Map updateAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        UserSession loginUser = SessionContextHelper.getLoginUser();
        try {
            if (!file.isEmpty()) {
                String contextPath = request.getContextPath();
                String avatar = FileUploadUtil.upload(Global.getAvatarPath(), file);
                sysUserService.updateMyAvatar(loginUser.getUserId(),contextPath + avatar);

                Map ajax = new HashMap();
                ajax.put("fileName", file.getOriginalFilename());
                ajax.put("url", contextPath + avatar);
                ajax.put("url2", avatar);
                return ajax;
            } else {
                throw BusinessException.build("头像不能为空");
            }
        } catch (Exception e) {
            log.error("修改头像失败！", e);
            throw BusinessException.build(e.getMessage());
        }
    }
}
