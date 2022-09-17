package one.yiran.dashboard.job;

import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.service.SysUserOnlineService;
import one.yiran.dashboard.util.UserCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

//在线->离线->删除
@ConditionalOnProperty(name = "dashboard.job.enable",havingValue = "true")
@Component
@Slf4j
public class UserOnlineCheckJob {

    @Autowired
    private SysUserOnlineService sysUserOnlineService;

    @PostConstruct
    void init(){
        log.info("处理用户是否在线定时任务初始化成功");
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void excuteTask() {
        log.info("处理用户是否在线 start");
        long start = System.currentTimeMillis();
        process();
        long end = System.currentTimeMillis();
        long gap = (end - start)/1000;
        log.info("处理用户是否在线 end 耗时:{}s",gap);
    }

    private void process() {
        Long sessionTimeout = Global.getSessionTimeout();
        log.info("系统当前的session有效期设置为{}",sessionTimeout);
        if(sessionTimeout == null)
            return;
        //设置为离线
        sysUserOnlineService.updateExpireUserOffline(sessionTimeout);
    }

}
