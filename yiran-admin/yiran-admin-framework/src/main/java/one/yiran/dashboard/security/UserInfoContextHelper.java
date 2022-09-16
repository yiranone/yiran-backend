package one.yiran.dashboard.security;

import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.util.UserCacheUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class UserInfoContextHelper {

    public static boolean isLogin() {
        return getLoginUser() != null;
    }

    public static String getCurrentLoginName() {
        return getLoginUser() == null ? null : getLoginUser().getLoginName();
    }

    public static Long getCurrentUserId() {
        return getLoginUser() == null ? null : getLoginUser().getUserId();
    }

    public static Long getChannelId() {
        return getLoginUser() == null ? null : getLoginUser().getChannelId();
    }
    public static Long getChannelIdWithCheck() {
        Long channelId = getChannelId();
        if(channelId == null)
            throw BusinessException.build("登陆用户渠道号不能为空");
        return channelId;
    }

    public static String getIp() {
        return getLoginUser() == null ? null : getLoginUser().getLoginIp();
    }


    public static String getCurrentPhoneNumber() {
        return getLoginUser() == null ? null : getLoginUser().getPhoneNumber();
    }

    public static UserSession getLoginUser() {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        Object o = request.getAttribute("YIRAN_LOGIN_USER");
        if(o != null) {
            return (UserSession)o;
        }
        UserSession user = UserCacheUtil.getSessionInfo(request);
        request.setAttribute("YIRAN_LOGIN_USER", user);
        return user;
    }

    public static void checkScopePermission(String perm, Long deptId){
        //getLoginUser().checkScopePermission(perm,deptId);
    }
}
