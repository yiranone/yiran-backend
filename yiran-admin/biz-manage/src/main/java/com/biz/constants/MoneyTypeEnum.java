package com.biz.constants;

import one.yiran.common.exception.BusinessException;

import java.util.Arrays;

public enum MoneyTypeEnum {
    UNKNOWN(-1, "未知类型"),

    DEPOSIT_MONEY(1, "充值"),
    WITHDRAW_MONEY(2, "提现"),
    TRANSFER_MONEY(3, "转账"),
    ORDER_LEVEL_PAY(4, "购买段位等级"),
    RETURN_MONEY_FOR_INVITE(5, "推荐奖励"),
    RETURN_MONEY_FOR_TEAM(6, "团队奖励"),
    RETURN_MONEY(7, "产出收益"),
    ;

    int type;
    String description;

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    MoneyTypeEnum(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public static String getDescriptionByState(int type) {
        return Arrays.stream(MoneyTypeEnum.values()).filter(e -> e.type == type).findFirst()
                .orElseThrow(() -> BusinessException.build("")).description;
    }
}
