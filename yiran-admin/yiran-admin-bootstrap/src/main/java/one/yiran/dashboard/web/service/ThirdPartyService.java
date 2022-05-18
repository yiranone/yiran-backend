package one.yiran.dashboard.web.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.web.config.AliCloudProperties;
import one.yiran.dashboard.web.response.ali.SmsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ThirdPartyService {
    private AliCloudProperties properties;

    @Autowired
    public ThirdPartyService(AliCloudProperties properties) {
        this.properties = properties;
    }

    public String sendSms(String phoneNumber, JSONObject params, Boolean resetPasswordSms) {
//        String paramString = params.toString();
//        SmsResponse response = AliCloudUtils.instance.sendSms(properties, phoneNumber, paramString, resetPasswordSms);
//        if (!response.isSuccess()) {
//            log.info("{} 发送短信异常，阿里云响应:{}", phoneNumber, response.getMessage());
//            throw BusinessException.build("短信平台应答:" + response.getMessage());
//        }
        return null;
    }
}
