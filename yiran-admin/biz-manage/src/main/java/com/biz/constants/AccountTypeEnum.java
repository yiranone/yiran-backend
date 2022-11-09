package com.biz.constants;

import one.yiran.common.exception.BusinessException;

import java.util.Arrays;

public enum AccountTypeEnum {

    MONEY(1), // 通证
    SCORE(2); // 积分
    int code;
    AccountTypeEnum(int code){
        this.code  = code;
    }

    public int getCode() {
        return code;
    }

    public static AccountTypeEnum getEnumByState(int state) {
        return Arrays.stream(AccountTypeEnum.values()).filter(e->e.code==state).findFirst()
                .orElseThrow(()-> BusinessException.build(""));
    }
}
