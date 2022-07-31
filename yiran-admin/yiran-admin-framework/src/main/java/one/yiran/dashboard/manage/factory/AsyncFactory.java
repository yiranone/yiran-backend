package one.yiran.dashboard.manage.factory;

import eu.bitwalker.useragentutils.UserAgent;
import one.yiran.dashboard.common.constants.SystemConstants;
import one.yiran.dashboard.manage.entity.SysLoginInfo;
import one.yiran.dashboard.manage.entity.SysOperateLog;
import one.yiran.dashboard.manage.service.SysLoginInfoService;
import one.yiran.dashboard.manage.service.SysOperLogService;
import one.yiran.dashboard.common.util.IpUtil;
import one.yiran.dashboard.common.util.LogUtil;
import one.yiran.dashboard.common.util.ServletUtil;
import one.yiran.dashboard.common.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimerTask;


public class AsyncFactory {
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    public static TimerTask recordOperateInfo(final SysOperateLog sysOperateLog) {
        return new TimerTask() {
            @Override
            public void run() {
                sysOperateLog.setJsonResult(StringUtils.substring(sysOperateLog.getJsonResult(),0,2048));
                sysOperateLog.setOperateLocation(IpUtil.getRealAddressByIP(sysOperateLog.getOperateIp()));
                sysOperateLog.setCreateTime(new Date());
                SpringUtil.getBean(SysOperLogService.class).insert(sysOperateLog);
            }
        };
    }

    public static TimerTask recordLoginInfo(final String username, final String status, final String message, final Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtil.getRequest().getHeader("User-Agent"));
        final String ip = IpUtil.getIpAddr(ServletUtil.getRequest());
        return new TimerTask() {
            @Override
            public void run() {
                String ipString = IpUtil.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(LogUtil.getBlock(ip));
                s.append(ipString);
                s.append(LogUtil.getBlock(username));
                s.append(LogUtil.getBlock(status));
                s.append(LogUtil.getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                SysLoginInfo logininfor = new SysLoginInfo();
                logininfor.setCreateBy(username);
                logininfor.setCreateTime(new Date());
                logininfor.setLoginName(username);
                logininfor.setIpAddr(ip);
                logininfor.setLoginLocation(ipString);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setMsg(message);
                logininfor.setLoginTime(new Date());
                // 日志状态
                if (SystemConstants.SUCCESS.equals(status) || SystemConstants.LOGOUT.equals(status) || SystemConstants.REGISTER.equals(status)) {
                    logininfor.setStatus(SystemConstants.SUCCESS);
                } else if (SystemConstants.FAIL.equals(status)) {
                    logininfor.setStatus(SystemConstants.FAIL);
                }
                // 插入数据
                SpringUtil.getBean(SysLoginInfoService.class).insert(logininfor);

            }
        };
    }
}
