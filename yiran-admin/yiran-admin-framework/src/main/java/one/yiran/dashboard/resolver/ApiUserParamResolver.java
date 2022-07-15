package one.yiran.dashboard.resolver;

import one.yiran.dashboard.common.annotation.ApiSessionAdmin;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.util.UserCacheUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class ApiUserParamResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.hasParameterAnnotation(ApiSessionAdmin.class)) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
								  ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
								  WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

		String token = httpServletRequest.getHeader(Global.getAuthKey());
		if (StringUtils.isBlank(token)) {
			return null;
		}

		UserSession u = UserCacheUtil.getSessionInfo(token);
		return u;
	}
}
