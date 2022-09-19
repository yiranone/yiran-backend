package one.yiran.dashboard.interceptor;

import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.util.ServletUtil;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns="/*")
@Order(2)
public class CacheRequestFilter implements Filter {

    @Override
    public void destroy() {
        log.info("销毁CacheRequestFilter");
    }

    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) sRequest;
        boolean isAjax = ServletUtil.isAjaxRequest(request);
        request.setAttribute("IS_AJAX",isAjax);
        if(isAjax) {
            ContentCachingRequestWrapper contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
            byte[] bytes = contentCachingRequestWrapper.getBody();
            String requestJson = new String(bytes,request.getCharacterEncoding());
            request.setAttribute("REQ_JSON",requestJson);
            sRequest = contentCachingRequestWrapper;
        }
        chain.doFilter(sRequest, sResponse);
    }

    @Override
    public void init(FilterConfig f) {
        log.info("初始化CacheRequestFilter");
    }
}