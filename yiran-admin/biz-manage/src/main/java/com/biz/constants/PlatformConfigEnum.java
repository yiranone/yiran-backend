package com.biz.constants;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum PlatformConfigEnum {
    NOTICE_SWITCH("通知开关"),
    DEFAULT_HEAD_IMG("默认头像"),

    SMS_DOMAIN("阿里云短信域名"),
    SMS_VERSION("短信版本号"),
    SMS_REGIONId("短信服务地域"),
    SMS_ACCESS_KEY_ID("短信AccessKeyID"),
    SMS_ACCESS_KEY_SECRET("短信AccessKeySecret"),
    SMS_SIGN_NAME("短信签名"),
    SMS_TEMPLATE("短信模版"),
    SMS_RESET_PASSWORD_TEMPLATE("短信重置密码模版"),
    SMS_GLOBE_TEMPLATE("短信国际模版"),
    ;

    @Getter
    String desc;
    PlatformConfigEnum(String desc){
        this.desc = desc;
    }

    public static String getDescByName(String name) {
        if(StringUtils.isBlank(name))
            return "";
        return Arrays.stream(PlatformConfigEnum.values()).filter(e->e.name().equals(name)).findFirst().get().getDesc();
    }
}
