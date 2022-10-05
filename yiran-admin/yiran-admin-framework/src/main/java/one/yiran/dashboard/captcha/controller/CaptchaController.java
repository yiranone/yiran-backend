package one.yiran.dashboard.captcha.controller;

import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.captcha.model.common.ResponseModel;
import one.yiran.dashboard.captcha.model.vo.CaptchaVO;
import one.yiran.dashboard.captcha.service.impl.DefaultCaptchaService;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@AjaxWrapper
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired(required = false)
    private DefaultCaptchaService captchaService;

    @RequestMapping("/get")
    public Object get(@RequestBody CaptchaVO data, HttpServletRequest request) {
        assert request.getRemoteHost()!=null;
        data.setBrowserInfo(getRemoteId(request));
        ResponseModel model = captchaService.get(data);
        if(!model.isSuccess()){
            throw BusinessException.build(model.getRepMsg());
        }
        return model.getRepData();
    }

    @RequestMapping("/check")
    public Object check(@RequestBody CaptchaVO data, HttpServletRequest request) {
        data.setBrowserInfo(getRemoteId(request));
        ResponseModel model = captchaService.check(data);
        if(!model.isSuccess()){
            throw BusinessException.build(model.getRepMsg());
        }
        return model.getRepData();
    }

    //@PostMapping("/verify")
    public Object verify(@RequestBody CaptchaVO data, HttpServletRequest request) {
        ResponseModel model = captchaService.verification(data);
        if(!model.isSuccess()){
            throw BusinessException.build(model.getRepMsg());
        }
        return model.getRepData();
    }

    public static final String getRemoteId(HttpServletRequest request) {
        String xfwd = request.getHeader("X-Forwarded-For");
        String ip = getRemoteIpFromXfwd(xfwd);
        String ua = request.getHeader("user-agent");
        if (StringUtils.isNotBlank(ip)) {
            return ip + ua;
        }
        return request.getRemoteAddr() + ua;
    }

    private static String getRemoteIpFromXfwd(String xfwd) {
        if (StringUtils.isNotBlank(xfwd)) {
            String[] ipList = xfwd.split(",");
            return StringUtils.trim(ipList[0]);
        }
        return null;
    }

}
