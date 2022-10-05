package one.yiran.dashboard.captcha.service.impl;


import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.cache.DashboardCacheService;
import one.yiran.dashboard.captcha.model.common.RepCodeEnum;
import one.yiran.dashboard.captcha.model.common.ResponseModel;
import one.yiran.dashboard.captcha.model.vo.CaptchaVO;
import one.yiran.dashboard.captcha.service.CaptchaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static one.yiran.dashboard.captcha.service.impl.AbstractCaptchaService.REDIS_SECOND_CAPTCHA_KEY;

@Slf4j
@Component
public class DefaultCaptchaService{

    @Autowired(required = false)
    private CaptchaService captchaService;

    public ResponseModel get(CaptchaVO captchaVO) {
        if(captchaService == null)
            throw BusinessException.build("系统不需要验证码");
        if (captchaVO == null) {
            return RepCodeEnum.NULL_ERROR.parseError("captchaVO");
        }
        if (StringUtils.isEmpty(captchaVO.getCaptchaType())) {
            return RepCodeEnum.NULL_ERROR.parseError("类型");
        }
        return captchaService.get(captchaVO);
    }

    public ResponseModel check(CaptchaVO captchaVO) {
        if (captchaVO == null) {
            return RepCodeEnum.NULL_ERROR.parseError("captchaVO");
        }
        if (StringUtils.isEmpty(captchaVO.getCaptchaType())) {
            return RepCodeEnum.NULL_ERROR.parseError("类型");
        }
        if (StringUtils.isEmpty(captchaVO.getToken())) {
            return RepCodeEnum.NULL_ERROR.parseError("token");
        }
        return captchaService.check(captchaVO);
    }

    public ResponseModel verification(CaptchaVO captchaVO) {
        if (captchaVO == null) {
            return RepCodeEnum.NULL_ERROR.parseError("captchaVO");
        }
        if (StringUtils.isEmpty(captchaVO.getCaptchaVerification())) {
            return RepCodeEnum.NULL_ERROR.parseError("二次校验参数");
        }
        DashboardCacheService cacheService = DashboardCacheService.getCacheService();
        try {
            String codeKey = String.format(REDIS_SECOND_CAPTCHA_KEY, captchaVO.getCaptchaVerification());
            if (!cacheService.exists(codeKey)) {
                return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_INVALID);
            }
            //二次校验取值后，即刻失效
            cacheService.delete(codeKey);
        } catch (Exception e) {
            log.error("验证码坐标解析失败", e);
            return ResponseModel.errorMsg(e.getMessage());
        }
        return ResponseModel.success();
    }

    public boolean verification(String captchaVerification) {
        DashboardCacheService cacheService = DashboardCacheService.getCacheService();
        try {
            String codeKey = String.format(REDIS_SECOND_CAPTCHA_KEY, captchaVerification);
            boolean ex = cacheService.exists(codeKey);
            //二次校验取值后，即刻失效
            if(ex) {
                cacheService.delete(codeKey);
            }
            return ex;
        } catch (Exception e) {
            log.error("验证码坐标解析失败", e);
            return false;
        }
    }

}
