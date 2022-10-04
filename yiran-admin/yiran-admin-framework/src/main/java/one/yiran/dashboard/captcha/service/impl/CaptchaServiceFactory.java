package one.yiran.dashboard.captcha.service.impl;

import one.yiran.dashboard.captcha.model.common.CaptchaTypeEnum;
import one.yiran.dashboard.captcha.model.common.Const;
import one.yiran.dashboard.captcha.service.CaptchaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class CaptchaServiceFactory {

    private static Logger logger = LoggerFactory.getLogger(CaptchaServiceFactory.class);

    public static CaptchaService getInstance(Properties config) {
        //先把所有CaptchaService初始化，通过init方法，实例字体等，add by lide1202@hotmail.com
        /*try{
            for(CaptchaService item: instances.values()){
                item.init(config);
            }
        }catch (Exception e){
            logger.warn("init captchaService fail:{}", e);
        }*/

        String captchaType = config.getProperty(Const.CAPTCHA_TYPE, "default");
//        CaptchaService ret = SpringUtil.getBean(CaptchaService.class);
        CaptchaService ret = instances.get(captchaType);
        if(ret != null)
            return  ret;
        if(StringUtils.equals(captchaType, CaptchaTypeEnum.BLOCKPUZZLE.getCodeValue())) {
            ret = new BlockPuzzleCaptchaServiceImpl();
        } else if(StringUtils.equals(captchaType, CaptchaTypeEnum.CLICKWORD.getCodeValue())) {
            ret = new ClickWordCaptchaServiceImpl();
        } else {
            throw new RuntimeException("unsupported-[captcha.type]=" + captchaType);
        }
        if (ret == null) {
            throw new RuntimeException("unsupported-[captcha.type]=" + captchaType);
        }
        ret.init(config);
        instances.put(captchaType,ret);
        return ret;
    }

    public volatile static Map<String, CaptchaService> instances = new HashMap();
}
