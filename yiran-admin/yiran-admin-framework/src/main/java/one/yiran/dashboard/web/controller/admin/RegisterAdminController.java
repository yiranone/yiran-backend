package one.yiran.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.entity.SysUser;
import one.yiran.dashboard.security.service.SysRegisterService;
import one.yiran.dashboard.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class RegisterAdminController {

    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private SysConfigService configService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    @AjaxWrapper
    public void ajaxRegister(SysUser user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            throw BusinessException.build("当前系统没有开启注册功能！");
        }
        registerService.register(user);
    }
}
