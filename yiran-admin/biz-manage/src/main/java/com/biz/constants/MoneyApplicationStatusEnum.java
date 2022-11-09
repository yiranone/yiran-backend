package com.biz.constants;

import java.util.Arrays;

public enum MoneyApplicationStatusEnum {
    UNKNOWN(-1, "未知状态"),
    APPLY(0, "申请"),
    PASS(1, "批准"),
    REJECT(2, "拒绝"),
    ERROR(3, "失败"),
    TODO(4, "处理中"),
    FINISH(5, "完成"),
    ;

    int type;
    String description;

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    MoneyApplicationStatusEnum(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public static String getDescByState(int type) {
        return Arrays.stream(MoneyApplicationStatusEnum.values()).filter(e -> e.type == type).findFirst()
                .orElse(UNKNOWN).description;
    }

    public static MoneyApplicationStatusEnum getByType(int type) {
        return Arrays.stream(MoneyApplicationStatusEnum.values()).filter(e -> e.type == type).findFirst()
                .orElse(null);
    }

}
