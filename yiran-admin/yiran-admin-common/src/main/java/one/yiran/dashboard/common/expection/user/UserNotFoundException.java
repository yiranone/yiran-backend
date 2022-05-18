package one.yiran.dashboard.common.expection.user;

public class UserNotFoundException extends UserException {


    public static final int KEY = 400;

    public UserNotFoundException() {
        super(KEY, "用户不存在");
    }

    public UserNotFoundException(String loginName) {
        super(KEY, "用户不存在，登陆名称:" + loginName,loginName);
    }

    public UserNotFoundException(Long userId) {
        super(KEY, "用户不存在，用户ID:" + userId);
    }
}
