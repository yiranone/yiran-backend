package com.biz.constants;

public class BizPermissionConstants {

    public static final String EXT_PREFIX = "biz";

    // 业务模块权限
    public static class Member {
        public static final String PREFIX = EXT_PREFIX + ":member:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
    }

    public static class MemberMoney {
        public static final String PREFIX = EXT_PREFIX + ":memberMoney:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
    }

    public static class PlatformParamConfig {
        public static final String PREFIX = EXT_PREFIX + ":platformParamConfig:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
    }

}
