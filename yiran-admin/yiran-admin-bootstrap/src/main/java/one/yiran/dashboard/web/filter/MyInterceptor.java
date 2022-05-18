package one.yiran.dashboard.web.filter;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
            Exception {
        //System.out.println("MyInterceptor1 action之前执行！！！");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView
            modelAndView) throws Exception {
//        System.out.println("MyInterceptor1 action执行之后，生成视图之前执行！！");
//        if(response.getStatus()==500){
//            throw BusinessException.build("服务器异常");
//        } else if(response.getStatus()==404){
//            throw BusinessException.build("请求不存在");
//        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        System.out.println("MyInterceptor1 最后执行！！！一般用于释放资源！！");
    }
}