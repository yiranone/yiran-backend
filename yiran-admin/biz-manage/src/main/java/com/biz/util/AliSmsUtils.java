package com.biz.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.biz.constants.PlatformConfigEnum;
import com.biz.vo.SmsResponse;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.util.MemberCacheUtil;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class AliSmsUtils {

    public static final String DEFAULT_COUNTRY_CODE = "86";

    public static SmsResponse sendSms(String channelCode, String countryCode, String phoneNumber, String jsonString, String usePasswordResetTempate) {
        if(StringUtils.isBlank(countryCode))
            throw new RuntimeException("发送短信失败,短信所属国家异常异常");
        if(!StringUtils.equalsAnyIgnoreCase(usePasswordResetTempate, "register", "resetPass")) {
            throw new RuntimeException("发送短信失败,短信类型异常");
        }
        String sms_template = StringUtils.equalsIgnoreCase("resetPass",usePasswordResetTempate) ?
                cacheValue(channelCode, PlatformConfigEnum.SMS_RESET_PASSWORD_TEMPLATE) :
                cacheValue(channelCode, PlatformConfigEnum.SMS_TEMPLATE);
        if (!DEFAULT_COUNTRY_CODE.equals(countryCode)) {//国际短信
            sms_template = cacheValue(channelCode, PlatformConfigEnum.SMS_GLOBE_TEMPLATE);
            phoneNumber = countryCode + phoneNumber;
        } else {

        }
        return sendSms(channelCode, phoneNumber, jsonString, sms_template);
    }

    public static SmsResponse sendSms(String channelCode, String phoneNumber, String jsonString, String sms_template) {
        String reginId = cacheValue(channelCode, PlatformConfigEnum.SMS_REGIONId);
        log.info("发送短信:channelCode={} phoneNumber={} jsonString={} sms_template={} reginId={}",channelCode,phoneNumber,jsonString,sms_template,reginId);
        if(StringUtils.isBlank(reginId))
            throw new RuntimeException("发送短信失败,SMS_REGIONId没有配置");
        DefaultProfile profile = DefaultProfile.getProfile(
                reginId,
                cacheValue(channelCode, PlatformConfigEnum.SMS_ACCESS_KEY_ID),
                cacheValue(channelCode, PlatformConfigEnum.SMS_ACCESS_KEY_SECRET));
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(cacheValue(channelCode, PlatformConfigEnum.SMS_DOMAIN));
        request.setVersion(cacheValue(channelCode, PlatformConfigEnum.SMS_VERSION));
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", cacheValue(channelCode, PlatformConfigEnum.SMS_REGIONId));
        request.putQueryParameter("PhoneNumbers", phoneNumber);
        request.putQueryParameter("SignName", cacheValue(channelCode, PlatformConfigEnum.SMS_SIGN_NAME));
        request.putQueryParameter("TemplateCode", sms_template);
        if (!StringUtils.isEmpty(jsonString)) {
            request.putQueryParameter("TemplateParam", jsonString);
        }

        try {
            CommonResponse response = client.getCommonResponse(request);
            String responseData = response.getData();
            log.info("sms response " + responseData);
            return JSON.parseObject(responseData, SmsResponse.class);
        } catch (ServerException e) {
            log.error("sms server error", e);
            throw new RuntimeException(e);
        } catch (ClientException e) {
            log.error("sms client error", e);
            throw new RuntimeException(e);
        }
    }

    private static String cacheValue(String channelCode, PlatformConfigEnum keyEnum){
        return MemberCacheUtil.getSystemConfig(channelCode, keyEnum.name());
    }

}
