package one.yiran.dashboard.captcha.config;

import one.yiran.dashboard.captcha.model.common.Const;
import one.yiran.dashboard.captcha.properties.AjCaptchaProperties;
import one.yiran.dashboard.captcha.service.CaptchaService;
import one.yiran.dashboard.captcha.service.impl.CaptchaServiceFactory;
import one.yiran.dashboard.captcha.util.ImageUtils;
import one.yiran.dashboard.common.constants.Global;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class AjCaptchaServiceAutoConfiguration {

    private static Logger logger = LoggerFactory.getLogger(AjCaptchaServiceAutoConfiguration.class);

    @Bean(name = "captchaConfig")
    public Properties captchaService(AjCaptchaProperties prop) {
        logger.info("自定义配置项：{}", prop.toString());
        if(StringUtils.equals(Global.getCaptchaType(),"none")) {
            logger.info("系统没有配置验证码校验");
            return null;
        }
        Properties config = new Properties();
        config.put(Const.CAPTCHA_CACHETYPE, prop.getCacheType().name());
        config.put(Const.CAPTCHA_WATER_MARK, prop.getWaterMark());
        config.put(Const.CAPTCHA_FONT_TYPE, prop.getFontType());
//        config.put(Const.CAPTCHA_TYPE, prop.getType().getCodeValue());
        config.put(Const.CAPTCHA_TYPE, Global.getCaptchaType());
        config.put(Const.CAPTCHA_INTERFERENCE_OPTIONS, prop.getInterferenceOptions());
        config.put(Const.ORIGINAL_PATH_JIGSAW, prop.getJigsaw());
        config.put(Const.ORIGINAL_PATH_PIC_CLICK, prop.getPicClick());
        config.put(Const.CAPTCHA_SLIP_OFFSET, prop.getSlipOffset());
        config.put(Const.CAPTCHA_AES_STATUS, String.valueOf(prop.getAesStatus()));
        config.put(Const.CAPTCHA_WATER_FONT, prop.getWaterFont());
        config.put(Const.CAPTCHA_CACAHE_MAX_NUMBER, prop.getCacheNumber());
        config.put(Const.CAPTCHA_TIMING_CLEAR_SECOND, prop.getTimingClear());

        config.put(Const.HISTORY_DATA_CLEAR_ENABLE, prop.isHistoryDataClearEnable() ? "1" : "0");

        config.put(Const.REQ_FREQUENCY_LIMIT_ENABLE, prop.getReqFrequencyLimitEnable() ? "1" : "0");
        config.put(Const.REQ_GET_LOCK_LIMIT, prop.getReqGetLockLimit() + "");
        config.put(Const.REQ_GET_LOCK_SECONDS, prop.getReqGetLockSeconds() + "");
        config.put(Const.REQ_GET_MINUTE_LIMIT, prop.getReqGetMinuteLimit() + "");
        config.put(Const.REQ_CHECK_MINUTE_LIMIT, prop.getReqCheckMinuteLimit() + "");
        config.put(Const.REQ_VALIDATE_MINUTE_LIMIT, prop.getReqVerifyMinuteLimit() + "");

        config.put(Const.CAPTCHA_FONT_SIZE, prop.getFontSize() + "");
        config.put(Const.CAPTCHA_FONT_STYLE, prop.getFontStyle() + "");
        config.put(Const.CAPTCHA_WORD_COUNT, prop.getClickWordCount() + "");

//        if ((StringUtils.isNotBlank(prop.getJigsaw()) && prop.getJigsaw().startsWith("classpath:"))
//                || (StringUtils.isNotBlank(prop.getPicClick()) && prop.getPicClick().startsWith("classpath:"))) {
//            //自定义resources目录下初始化底图
//            config.put(Const.CAPTCHA_INIT_ORIGINAL, "true");
//            initializeBaseMap(prop.getJigsaw(), prop.getPicClick());
//        }
//        CaptchaService s = CaptchaServiceFactory.getInstance(config);
//        return s;

        return config;
    }

//    private static void initializeBaseMap(String jigsaw, String picClick) {
//        ImageUtils.cacheBootImage(getResourcesImagesFile(jigsaw + "/original/*.png"),
//                getResourcesImagesFile(jigsaw + "/slidingBlock/*.png"),
//                getResourcesImagesFile(picClick + "/*.png"));
//    }

//    public static Map<String, String> getResourcesImagesFile(String path) {
//        Map<String, String> imgMap = new HashMap<>();
//        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        try {
//            Resource[] resources = resolver.getResources(path);
//            for (Resource resource : resources) {
//                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
//                String string = Base64Utils.encodeToString(bytes);
//                String filename = resource.getFilename();
//                imgMap.put(filename, string);
//            }
//        } catch (Exception e) {
//            logger.error("加载资源失败",e);
//        }
//        return imgMap;
//    }
}
