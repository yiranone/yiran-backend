package one.yiran.dashboard.security.config;

public class PermissionConstants {

    public static final String SYSTEM_PREFIX = "system";

    public static class User {
        public static final String PREFIX = SYSTEM_PREFIX + ":user:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
        public static final String EXPORT = PREFIX + "export";
        public static final String IMPORT = PREFIX + "import";
        public static final String RESET_PWD = PREFIX + "resetPwd";
        public static final String UNLOCK = PREFIX + "unlock";
        public static final String AUTH = PREFIX + "auth";
    }

    public static class Dept {
        public static final String PREFIX = SYSTEM_PREFIX + ":dept:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
    }

    public static class Config {
        public static final String PREFIX = SYSTEM_PREFIX + ":config:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
        public static final String EXPORT = PREFIX + "export";
    }

    public static class Channel {
        public static final String NAME = "渠道管理";
        public static final String PREFIX = SYSTEM_PREFIX + ":channel:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
        public static final String EXPORT = PREFIX + "export";
    }

    public static class Notice {
        public static final String PREFIX = SYSTEM_PREFIX + ":notice:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
        public static final String EXPORT = PREFIX + "export";
    }

    public static class Post {
        public static final String PREFIX = SYSTEM_PREFIX + ":post:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
    }

    public static class Perm {
        public static final String PREFIX = SYSTEM_PREFIX + ":perm:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
    }

    public static class Role {
        public static final String PREFIX = SYSTEM_PREFIX + ":role:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
        public static final String EXPORT = PREFIX + "export";
        public static final String AUTH = PREFIX + "auth";
    }

    public static class Menu {
        public static final String PREFIX = SYSTEM_PREFIX + ":menu:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
    }

    public static class Dict {
        public static final String PREFIX = SYSTEM_PREFIX + ":dict:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String DELETE = PREFIX + "delete";
        public static final String ADD = PREFIX + "add";
        public static final String EXPORT = PREFIX + "export";
    }

    public static class OperateLog {
        public static final String PREFIX = SYSTEM_PREFIX + ":operate:log:";
        public static final String VIEW = PREFIX + "view";
        public static final String DELETE = PREFIX + "delete";
        public static final String EXPORT = PREFIX + "export";
    }

    public static class LoginInfo {
        public static final String PREFIX = SYSTEM_PREFIX + ":login:info:";
        public static final String VIEW = PREFIX + "view";
        public static final String DELETE = PREFIX + "delete";
        public static final String EXPORT = PREFIX + "export";
    }

    public static class UserOnline {
        public static final String PREFIX = SYSTEM_PREFIX + ":online:";
        public static final String VIEW = PREFIX + "view";
        public static final String FORCE_LOGOUT = PREFIX + "forceLogout";
    }

}
