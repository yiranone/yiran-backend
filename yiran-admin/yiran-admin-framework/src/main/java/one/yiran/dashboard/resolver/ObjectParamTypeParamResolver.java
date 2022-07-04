package one.yiran.dashboard.resolver;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.ApiObject;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.db.common.util.ServletRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class ObjectParamTypeParamResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.hasParameterAnnotation(ApiObject.class)) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
								  ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
								  WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

		Type parameterType = parameter.getGenericParameterType();
		ApiObject apiParam = parameter.getParameterAnnotation(ApiObject.class);
		if(apiParam == null)
			return null;

		Object v = ServletRequestUtil.getObjectFromRequest(httpServletRequest, parameterType);
		if(apiParam.validate()) {
			List<String> res = valid(v);
			if(res.size() > 0) {
				String msg = String.join(",", res);
				throw BusinessException.build(msg);
			}
		}
		if(apiParam.createIfNull() && v == null) {
			return Class.forName(parameterType.getTypeName()).newInstance();
		}
		return v;
	}

	private <T> List<String> valid(T obj){
		Set<ConstraintViolation<T>> validRes = Validation
				.buildDefaultValidatorFactory()
				.getValidator()
				.validate(obj);
		List<String> res = validRes
				.stream()
				.map(e-> "" + e.getPropertyPath() +
				e.getMessage())
				.collect(Collectors.toList());
		return res;
	}
}
