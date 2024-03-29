package one.yiran.dashboard.interceptor;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.common.annotation.RequireUserLogin;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.expection.user.UserNotLoginException;
import one.yiran.dashboard.service.SysRoleService;
import one.yiran.dashboard.util.UserCacheUtil;
import one.yiran.dashboard.factory.AsyncManager;
import one.yiran.dashboard.service.SysUserOnlineService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.TimerTask;

@Component
@Slf4j
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private SysUserOnlineService sysUserOnlineService;
    @Autowired
    private SysRoleService sysRoleService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequireUserLogin requireUserLogin = null;
        RequirePermission requirePermission = null;
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            requireUserLogin = handlerMethod.getMethodAnnotation(RequireUserLogin.class);
            requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        }

        if (HttpMethod.OPTIONS.name().equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        UserSession session = null;
        String token = getTok(request);
        if(StringUtils.isNotBlank(token)) {
            session = UserCacheUtil.getSessionInfo(token);
            if (session != null) {
                //用户登陆了，修改下在线用户列表
                String sessionId = session.getToken();
                Long lastSync = session.getLastSyncToDbTime();
                long now = new Date().getTime();

                //每10s做一次持久化，异步在数据库写入用户在线
                if (sessionId != null && (lastSync == null || lastSync + 10 * 1000 < now)) {
                    session.setLastSyncToDbTime(now);
                    UserCacheUtil.setSessionInfo(sessionId, session);
                    AsyncManager.me().execute(new TimerTask() {
                        @Override
                        public void run() {
                            sysUserOnlineService.refreshUserLastAccessTime(sessionId, new Date());
                        }
                    });
                }
            }
        }

        // 忽略没有被注解的请求, 不做后续token认证校验
        if (requireUserLogin == null && requirePermission == null) {
            return true;
        }

        if (token == null) {
            throw new UserNotLoginException();
        }

        if (session == null) {
            throw new UserNotLoginException("用户未登录,或者登录已经过期");
        } else if (session.getIsLocked() == Boolean.TRUE) {
            throw BusinessException.build("您的账户被冻结,请联系客服");
        }

        if (requirePermission != null) {
            if (session.isAdmin()) {
                //是管理员 忽略权限校验
            } else {
                String[] perms = requirePermission.value();
                if (perms != null && perms.length > 0) {
                    //校验用户权限
                    boolean isAnd = requirePermission.isAnd();
                    if (isAnd) {
                        for (String perm : perms) {
                            boolean has = sysRoleService.checkUserHasPermission(session.getUserId(), perm);
                            if (!has) {
                                log.info("AND校验用户{}权限{}未通过,URI={}", session.getLoginName(), perm, request.getRequestURI());
                                throw BusinessException.build("用户缺少权限:" + perm);
                            } else {
                                log.info("AND校验用户{}权限{}通过,URI={}", session.getLoginName(), perm, request.getRequestURI());
                            }
                        }
                    } else {
                        boolean pass = false;
                        for (String perm : perms) {
                            boolean has = sysRoleService.checkUserHasPermission(session.getUserId(), perm);
                            if (!has) {
                                log.info("OR校验用户{}权限{}未通过,URI={}", session.getLoginName(), perm, request.getRequestURI());
                            } else {
                                log.info("OR校验用户{}权限{}通过,URI={}", session.getLoginName(), perm, request.getRequestURI());
                                pass = true;
                                break;
                            }
                        }
                        if (!pass) {
                            throw BusinessException.build("OR用户缺少任意一个权限:" + Arrays.toString(perms));
                        }
                    }
                }
            }
        }

        request.setAttribute("userId", session.getUserId());
        return true;
    }

    private String getTok(HttpServletRequest request) {
        return request.getHeader(Global.getAuthKey());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
}