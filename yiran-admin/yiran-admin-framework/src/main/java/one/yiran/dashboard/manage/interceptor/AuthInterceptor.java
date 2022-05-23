package one.yiran.dashboard.manage.interceptor;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.RequirePermission;
import one.yiran.dashboard.common.model.UserInfo;
import one.yiran.dashboard.common.annotation.RequireUserLogin;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.expection.user.UserNotLoginException;
import one.yiran.dashboard.common.util.UserCacheUtil;
import one.yiran.dashboard.manage.service.SysUserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequireUserLogin requireUserLogin = null;
        RequirePermission requirePermission = null;
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            requireUserLogin = handlerMethod.getMethodAnnotation(RequireUserLogin.class);
            requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        }

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 忽略没有被注解的请求, 不做后续token认证校验
        if (requireUserLogin == null && requirePermission == null) {
            return true;
        }

        String token = getTok(request);

        if(token == null) {
            throw new UserNotLoginException();
        }

        UserInfo session = null;
        if((session = UserCacheUtil.getSessionInfo(token)) == null) {
            throw new UserNotLoginException("用户未登录,或者登录已经过期");
        } else if (session.getIsLocked() == Boolean.TRUE) {
            throw BusinessException.build("您的账户被冻结,请联系客服");
        }

        if(requirePermission != null) {
           String[] perms = requirePermission.value();
           if(perms != null && perms.length > 0) {
               //校验用户权限
           }
        }

        request.setAttribute("userId", session.getUserId());
        return true;
    }

    private String getTok(HttpServletRequest request){
        return request.getHeader(Global.getAuthKey());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
}