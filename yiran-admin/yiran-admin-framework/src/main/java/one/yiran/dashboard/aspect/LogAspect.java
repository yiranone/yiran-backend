package one.yiran.dashboard.aspect;

import com.alibaba.fastjson.JSON;
import one.yiran.dashboard.common.constants.UserConstants;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.common.annotation.Log;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.util.MemberCacheUtil;
import one.yiran.dashboard.entity.SysOperateLog;
import one.yiran.dashboard.factory.AsyncManager;
import one.yiran.dashboard.factory.AsyncFactory;
import one.yiran.dashboard.common.util.IpUtil;
import one.yiran.dashboard.common.util.ServletUtil;
import one.yiran.dashboard.security.SessionContextHelper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

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
            UserSession currentUser = SessionContextHelper.getLoginUser();

            HttpServletRequest request = ServletUtil.getRequest();
            MemberSession memberSession = MemberCacheUtil.getSessionInfo(request);

            if (currentUser == null && memberSession == null)
                return;
            SysOperateLog sysOperateLog = new SysOperateLog();
            String loginName = "";
            if (currentUser != null) {
                sysOperateLog.setSessionType(UserConstants.SESSION_TYPE_ADMIN);
                loginName = currentUser.getLoginName();
            } else if (memberSession != null) {
                sysOperateLog.setSessionType(UserConstants.SESSION_TYPE_MEMBER);
                loginName = memberSession.getPhone();
            }
            // *========数据库日志=========*//
            sysOperateLog.setCreateBy(currentUser.getLoginName());
            sysOperateLog.setStatus(SystemConstants.SUCCESS);
            // 请求的地址
            if (request != null) {
                String ip = IpUtil.getIpAddr(request);
                sysOperateLog.setOperateIp(ip);
                sysOperateLog.setOperateLocation(IpUtil.getRealAddressByIP(ip));
            }
            // 返回参数
            if(jsonResult != null) {
                sysOperateLog.setJsonResult(JSON.toJSONString(jsonResult));
            }
            sysOperateLog.setOperateUrl(ServletUtil.getRequest().getRequestURI());
            sysOperateLog.setOperateName(loginName);
            if (StringUtils.isNotEmpty(currentUser.getDeptName())) {
                sysOperateLog.setDeptName(currentUser.getDeptName());
            }

            if (e != null) {
                sysOperateLog.setStatus(SystemConstants.FAIL);
                sysOperateLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            sysOperateLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            sysOperateLog.setRequestMethod(ServletUtil.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(controllerLog, sysOperateLog);
            // 保存数据库
            AsyncManager.me().execute(AsyncFactory.recordOperateInfo(sysOperateLog));
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
     * @param sysOperateLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(Log log, SysOperateLog sysOperateLog) throws Exception {
        // 设置action动作
        sysOperateLog.setBusinessType(log.businessType().getIndex());
        // 设置标题
        sysOperateLog.setTitle(log.title());
        // 设置操作人类别
        sysOperateLog.setOperatorType(log.operatorType().getIndex());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
//            Map<String, String[]> map = ServletUtil.getRequest().getParameterMap();
//            String params = JSON.toJSONString(map);

            String reqMessage = ServletUtil.getRequest().getAttribute("REQ_JSON") == null ? "" : ServletUtil.getRequest().getAttribute("REQ_JSON").toString();
            sysOperateLog.setOperateParam(StringUtils.substring(reqMessage, 0, 2000));
        }
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
