package one.yiran.dashboard.manage.aspect;

import com.alibaba.fastjson.JSON;
import one.yiran.dashboard.common.constants.UserConstants;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.common.model.AdminSession;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.common.util.MemberCacheUtil;
import one.yiran.dashboard.manage.entity.SysOperLog;
import one.yiran.dashboard.manage.factory.AsyncManager;
import one.yiran.dashboard.manage.factory.AsyncFactory;
import one.yiran.dashboard.common.util.IpUtil;
import one.yiran.dashboard.common.util.ServletUtil;
import one.yiran.dashboard.manage.security.UserInfoContextHelper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

@Aspect
//@Slf4j
@Component
public class LogAspect {
    // 配置织入点
    @Pointcut("@annotation(one.yiran.dashboard.common.annotation.Log)")
    public void logPointCut() {
    }

    @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult){
        try {
            // 获得注解
            Log controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return;
            }

            // 获取当前的用户
            AdminSession currentUser = UserInfoContextHelper.getLoginUser();

            HttpServletRequest request = ServletUtil.getRequest();
            MemberSession memberSession = MemberCacheUtil.getSessionInfo(request);

            if (currentUser == null && memberSession == null)
                return;
            SysOperLog sysOperLog = new SysOperLog();
            String loginName = "";
            if (currentUser != null) {
                sysOperLog.setSessionType(UserConstants.SESSION_TYPE_ADMIN);
                loginName = currentUser.getLoginName();
            } else if (memberSession != null) {
                sysOperLog.setSessionType(UserConstants.SESSION_TYPE_MEMBER);
                loginName = memberSession.getPhone();
            }
            // *========数据库日志=========*//
            sysOperLog.setCreateBy(currentUser.getLoginName());
            sysOperLog.setStatus(SystemConstants.SUCCESS);
            // 请求的地址
            if (request != null) {
                String ip = IpUtil.getIpAddr(request);
                sysOperLog.setOperIp(ip);
                sysOperLog.setOperLocation(IpUtil.getRealAddressByIP(ip));
            }
            // 返回参数
            if(jsonResult != null) {
                sysOperLog.setJsonResult(JSON.toJSONString(jsonResult));
            }
            sysOperLog.setOperUrl(ServletUtil.getRequest().getRequestURI());
            sysOperLog.setOperName(loginName);
            if (StringUtils.isNotEmpty(currentUser.getDeptName())) {
                sysOperLog.setDeptName(currentUser.getDeptName());
            }

            if (e != null) {
                sysOperLog.setStatus(SystemConstants.FAIL);
                sysOperLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            sysOperLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            sysOperLog.setRequestMethod(ServletUtil.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(controllerLog, sysOperLog);
            // 保存数据库
            AsyncManager.me().execute(AsyncFactory.recordOperateInfo(sysOperLog));
        } catch (Exception exp) {
            // 记录本地异常日志
            //log.error("==前置通知异常==");
            //log.error("异常信息:", exp);
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log     日志
     * @param sysOperLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(Log log, SysOperLog sysOperLog) throws Exception {
        // 设置action动作
        sysOperLog.setBusinessType(log.businessType().getIndex());
        // 设置标题
        sysOperLog.setTitle(log.title());
        // 设置操作人类别
        sysOperLog.setOperatorType(log.operatorType().getIndex());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(sysOperLog);
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param sysOperLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(SysOperLog sysOperLog) throws Exception {
        Map<String, String[]> map = ServletUtil.getRequest().getParameterMap();
        String params = JSON.toJSONString(map);
        sysOperLog.setOperParam(StringUtils.substring(params, 0, 2000));
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private Log getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(Log.class);
        }
        return null;
    }
}
