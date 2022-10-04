package one.yiran.dashboard.captcha.service.impl;


import one.yiran.dashboard.cache.DashboardCacheService;
import one.yiran.dashboard.captcha.model.common.RepCodeEnum;
import one.yiran.dashboard.captcha.model.common.ResponseModel;
import one.yiran.dashboard.captcha.model.vo.CaptchaVO;
import one.yiran.dashboard.captcha.service.CaptchaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class DefaultCaptchaServiceImpl extends AbstractCaptchaService{

    @Override
    public String captchaType() {
        return "default";
    }

    @Override
    public void init(Properties config) {
        for (String s : CaptchaServiceFactory.instances.keySet()) {
            if(captchaType().equals(s)){
                continue;
            }
            getService(s).init(config);
        }
    }

	@Override
	public void destroy(Properties config) {
		for (String s : CaptchaServiceFactory.instances.keySet()) {
			if(captchaType().equals(s)){
				continue;
			}
			getService(s).destroy(config);
		}
	}

	private CaptchaService getService(String captchaType){
        return CaptchaServiceFactory.instances.get(captchaType);
    }

    @Override
    public ResponseModel get(CaptchaVO captchaVO) {
        if (captchaVO == null) {
            return RepCodeEnum.NULL_ERROR.parseError("captchaVO");
        }
        if (StringUtils.isEmpty(captchaVO.getCaptchaType())) {
            return RepCodeEnum.NULL_ERROR.parseError("类型");
        }
        return getService(captchaVO.getCaptchaType()).get(captchaVO);
    }

    @Override
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
        return getService(captchaVO.getCaptchaType()).check(captchaVO);
    }

    @Override
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
            logger.error("验证码坐标解析失败", e);
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
            logger.error("验证码坐标解析失败", e);
            return false;
        }
    }

}
