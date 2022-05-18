package one.yiran.dashboard.manage.interceptor;

import one.yiran.dashboard.common.util.ServletUtil;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns="/*")
@Order(2)
public class CacheRequestFilter implements Filter {

    @Override
    public void destroy() {
        System.out.println("销毁CacheRequestFilter");
    }

    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) sRequest;
        boolean isAjax = ServletUtil.isAjaxRequest(request);
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
        System.out.println("初始化CacheRequestFilter");
    }
}