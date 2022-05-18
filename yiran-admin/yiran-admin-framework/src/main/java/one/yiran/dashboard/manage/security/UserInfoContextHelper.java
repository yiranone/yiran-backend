package one.yiran.dashboard.manage.security;

import one.yiran.dashboard.common.model.UserInfo;
import one.yiran.dashboard.common.util.UserCacheUtil;
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

//    public static String getSessionId() {
//        return SecurityUtils.getSubject().getSession() == null ? null : String.valueOf(SecurityUtils.getSubject().getSession().getId());
//    }

//    public static OnlineSession getSession() {
//        Subject subject = SecurityUtils.getSubject();
//        Session session = SpringUtil.getBean(SysShiroService.class).getSession(subject.getSession().getId());
//        OnlineSession onlineSession = (OnlineSession)session;
//        return onlineSession;
//    }

    public static String getIp() {
       // return getSubject().getSession().getHost();
        return null;
    }


    public static String getCurrentPhoneNumber() {
        return getLoginUser() == null ? null : getLoginUser().getPhoneNumber();
    }

//    public static SysUser getUser() {
//        SysUser user = null;
//        Object obj = getSubject().getPrincipal();
//        if (obj != null) {
//            user = new SysUser();
//            BeanUtils.copyProperties(obj, user);
//        }
//        return user;
//    }

    public static UserInfo getLoginUser() {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        Object o = request.getAttribute("YIRAN_LOGIN_USER");
        if(o != null) {
            return (UserInfo)o;
        }
        UserInfo user = UserCacheUtil.getSessionInfo(request);
        request.setAttribute("YIRAN_LOGIN_USER", user);

        //        Object obj = getSubject().getPrincipal();
//        if (obj != null) {
//            if(obj instanceof UserInfo) {
//                return (UserInfo)obj;
//            } else {
//                user = new UserInfo();
//                BeanUtils.copyProperties(obj, user);
//            }
//        }
        return user;
    }

    public static void checkScopePermission(String perm, Long deptId){
        //getLoginUser().checkScopePermission(perm,deptId);
    }

    public static void setUser(UserInfo user) {
//        Subject subject = getSubject();
//        PrincipalCollection principalCollection = subject.getPrincipals();
//        String realmName = principalCollection.getRealmNames().iterator().next();
//        PrincipalCollection newPrincipalCollection = new SimplePrincipalCollection(user, realmName);
//        // 重新加载Principal
//        subject.runAs(newPrincipalCollection);
    }

    //public static Subject getSubject() {
//        return SecurityUtils.getSubject();
//    }
}
