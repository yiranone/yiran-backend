package one.yiran.dashboard.web.filter;

import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.common.domain.ResponseContainer;
import one.yiran.dashboard.interceptor.HttpLogPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import java.util.List;

public class AjaxMethodReturnValueHandler extends AbstractMessageConverterMethodProcessor {

    public AjaxMethodReturnValueHandler(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return false;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
//        if(StringUtils.equals(returnType.getMethod().getReturnType().getName(), "org.springframework.web.context.request.async.DeferredResult")) {
//            return false;
//        }
        return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), AjaxWrapper.class) ||
                returnType.hasMethodAnnotation(AjaxWrapper.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        return null;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);
        ResponseContainer rs = ResponseContainer.successContainer(returnValue);

//        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(webRequest);
//        if (asyncManager.isConcurrentHandlingStarted()) {
//            return;
//        }

        //异步请求进来2次,第一次走下面这个逻辑，第二次不走
        if (returnValue instanceof DeferredResult) {
            DeferredResult<?> result = (DeferredResult<?>) returnValue;
            WebAsyncUtils.getAsyncManager(webRequest).startDeferredResultProcessing(result, mavContainer);
            return;
        }

        HttpLogPrinter.print(inputMessage.getServletRequest(),rs);

        writeWithMessageConverters(ResponseContainer.successContainer(returnValue), returnType, inputMessage, outputMessage);
        mavContainer.setRequestHandled(true);
    }
}
