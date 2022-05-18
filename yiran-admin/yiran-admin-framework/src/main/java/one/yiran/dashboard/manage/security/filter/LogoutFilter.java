//package one.yiran.dashboard.manage.security.filter;
//
//import lombok.extern.slf4j.Slf4j;
//import UserInfo;
//import ShiroConstants;
//import SystemConstants;
//import AsyncManager;
//import AsyncFactory;
//import MessageUtil;
//import UserInfoContextHelper;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.shiro.cache.Cache;
//import org.apache.shiro.cache.CacheManager;
//import org.apache.shiro.session.SessionException;
//import org.apache.shiro.subject.Subject;
//
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import java.io.Serializable;
//import java.util.Deque;
//
//@Slf4j
//public class LogoutFilter extends org.apache.shiro.web.filter.authc.LogoutFilter {
//
//    /**
//     * 退出后重定向的地址
//     */
//    private String loginUrl;
//
//    private Cache<String, Deque<Serializable>> cache;
//
//    public String getLoginUrl() {
//        return loginUrl;
//    }
//
//    public void setLoginUrl(String loginUrl) {
//        this.loginUrl = loginUrl;
//    }
//
//    @Override
//    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
//        try {
//            Subject subject = getSubject(request, response);
//            String redirectUrl = getRedirectUrl(request, response, subject);
//            try {
//                UserInfo user = UserInfoContextHelper.getLoginUser();
//                if (user != null) {
//                    String loginName = user.getLoginName();
//                    // 记录用户退出日志
//                    AsyncManager.me().execute(AsyncFactory.recordLoginInfo(loginName, SystemConstants.LOGOUT, MessageUtil.message("user.logout.success")));
//                    // 清理缓存
//                    if(cache != null)
//                        cache.remove(loginName);
//                }
//                // 退出登录
//                subject.logout();
//            } catch (SessionException ise) {
//                log.error("logout fail.", ise);
//            }
//            issueRedirect(request, response, redirectUrl);
//        } catch (Exception e) {
//            log.error("Encountered session exception during logout.  This can generally safely be ignored.", e);
//        }
//        return false;
//    }
//
//    /**
//     * 退出跳转URL
//     */
//    @Override
//    protected String getRedirectUrl(ServletRequest request, ServletResponse response, Subject subject) {
//        String url = getLoginUrl();
//        if (StringUtils.isNotEmpty(url)) {
//            return url;
//        }
//        return super.getRedirectUrl(request, response, subject);
//    }
//
//    // 设置Cache的key的前缀
//    public void setCacheManager(CacheManager cacheManager) {
//        // 必须和ehcache缓存配置中的缓存name一致
//        this.cache = cacheManager.getCache(ShiroConstants.SYS_USERCACHE);
//    }
//}
