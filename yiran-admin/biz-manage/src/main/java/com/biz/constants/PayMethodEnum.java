package com.biz.constants;

import lombok.Getter;
import one.yiran.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum PayMethodEnum {
    SYSTEM(0, "系统"), //系统操作
    MONEY(1, "通证"), // 客户操作
    SERVICE(2, "客服"), //客服操作
    ;
    int code;

    @Getter
    String desc;

    PayMethodEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public static PayMethodEnum getEnumByState(int state) {
        return Arrays.stream(PayMethodEnum.values()).filter(e -> e.code == state).findFirst()
                .orElseThrow(() -> BusinessException.build(""));
    }

    public static String getDescByState(int state) {
        return Arrays.stream(PayMethodEnum.values()).filter(e -> e.code == state).findFirst().get().getDesc();
    }

    public static String getDescByName(String name) {
        if (StringUtils.isBlank(name))
            return "";
        for (PayMethodEnum payMethodEnum : PayMethodEnum.values()) {
            if (payMethodEnum.name().equals(name)) {
                return payMethodEnum.getDesc();
            }
        }
        return name;
    }
}
