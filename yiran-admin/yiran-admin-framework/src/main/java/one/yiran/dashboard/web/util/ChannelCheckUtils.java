package one.yiran.dashboard.web.util;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.security.SessionContextHelper;

@Slf4j
public class ChannelCheckUtils {

    public static Long getChannelIdIfNotAdmin(){
        Long channelId = null;
        if(!Global.isAdmin(SessionContextHelper.getCurrentLoginName())) {
            channelId = SessionContextHelper.getChannelIdWithCheck();
        }
        return channelId;
    }

    public static void checkHasPermission(Long channelId) {
        if(Global.isAdmin(SessionContextHelper.getCurrentLoginName())) {
            return;
        }
        Long currentLoginUserChannelId  = SessionContextHelper.getChannelId();
        if (currentLoginUserChannelId == null) {
            throw BusinessException.build("当前登陆用户渠道为空");
        }
        if(!channelId.equals(currentLoginUserChannelId)) {
            log.error("用户{}渠道{}没有权限操作渠道{}的权限", SessionContextHelper.getCurrentLoginName() + " " +SessionContextHelper.getCurrentUserId(),currentLoginUserChannelId,channelId);
            throw BusinessException.build("当前登陆用户没有权限操作该对象");
        }
    }
}
