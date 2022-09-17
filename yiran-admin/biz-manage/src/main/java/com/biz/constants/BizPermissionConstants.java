package com.biz.constants;

public class BizPermissionConstants {

    public static final String EXT_PREFIX = "ext";

    // 业务模块权限
    public static class Member {
        public static final String PREFIX = EXT_PREFIX + ":member:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
    }

}
