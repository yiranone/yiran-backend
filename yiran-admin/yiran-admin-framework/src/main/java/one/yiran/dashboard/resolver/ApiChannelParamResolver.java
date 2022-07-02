package one.yiran.dashboard.resolver;

import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.ApiChannel;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.manage.entity.SysChannel;
import one.yiran.dashboard.manage.service.SysChannelService;
import one.yiran.dashboard.vo.ChannelVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class ApiChannelParamResolver implements HandlerMethodArgumentResolver {

	@Autowired
	private SysChannelService channelService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.hasParameterAnnotation(ApiChannel.class)) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
								  ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
								  WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

		ApiChannel apiChannel = parameter.getParameterAnnotation(ApiChannel.class);
		boolean required = apiChannel.required();
		String channelCode = httpServletRequest.getHeader(Global.getChannelKey());
		if (StringUtils.isBlank(channelCode)) {
			if(required)
				throw BusinessException.build("渠道号没传");
			return null;
		}
		SysChannel channel = channelService.selectByChannelCode(channelCode);
		if (channel == null && required) {
			throw BusinessException.build("渠道不存在:"+channelCode);
		}
		return ChannelVO.from(channel);
	}
}
