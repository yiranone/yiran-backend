package com.biz.interceptor;

import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.util.MemberCacheUtil;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.RequireMemberLogin;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.expection.user.UserNotLoginException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class MemberInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequireMemberLogin requireMemberLogin = null;

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            requireMemberLogin = handlerMethod.getMethodAnnotation(RequireMemberLogin.class);
        }

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 忽略没有被注解的请求, 不做后续token认证校验
        if (requireMemberLogin == null) {
            return true;
        }

        String token = getTok(request);

        if(StringUtils.isBlank(token )) {
            log.info("用户TOKEN为空");
            throw new UserNotLoginException();
        }

        MemberSession session = null;
        if((session = MemberCacheUtil.getSessionInfo(token)) == null) {
            log.info("用户没有登陆或者登录已经过期 {}",token);
            throw new UserNotLoginException("用户未登录,或者登录已经过期");
        } else if (session.getIsLocked() == Boolean.TRUE) {
            throw BusinessException.build("您的账户被冻结,请联系客服");
        }

        request.setAttribute("memberId", session.getMemberId());
        return true;
    }

    private String getTok(HttpServletRequest request){
        return request.getHeader(Global.getAuthKey());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
}