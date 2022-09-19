package one.yiran.dashboard.resolver;

import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.common.exception.BusinessException;
import one.yiran.db.common.util.ServletRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 */
public class SimpleParamTypeParamResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.hasParameterAnnotation(ApiParam.class)) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
								  ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
								  WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

		String parameterName = parameter.getParameterName();
		Type parameterType = parameter.getGenericParameterType();
		ApiParam apiParam = parameter.getParameterAnnotation(ApiParam.class);
		String apiName = apiParam.name();
		if(StringUtils.isNotBlank(apiName)) {
			parameterName = apiName;
		}

		Object v = ServletRequestUtil.getValueFromRequest(httpServletRequest, parameterName, parameterType);
		if(apiParam != null && apiParam.required()) {
			boolean isNull = false;
			if (String.class.getName().equals(parameterType.getTypeName()) &&
					(v ==null || StringUtils.isBlank(v.toString())) ) {
				isNull = true;
			} else if(v == null){
				isNull = true;
			}
			if(isNull)
				throw BusinessException.build("请求参数校验异常:" + parameterName + "不能为空");
		}
		if (v instanceof String) {
			return StringUtils.trim((String)v);
		}
		return v;
	}
}
