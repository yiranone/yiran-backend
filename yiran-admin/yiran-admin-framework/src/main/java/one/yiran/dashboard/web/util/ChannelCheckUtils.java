package one.yiran.dashboard.web.util;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.security.UserInfoContextHelper;

@Slf4j
public class ChannelCheckUtils {
    public static void checkHasPermission(Long channelId) {
        Long currentLoginUserChannelId  = UserInfoContextHelper.getChannelId();
        if (currentLoginUserChannelId == null) {
            throw BusinessException.build("当前登陆用户渠道为空");
        }
        if(!channelId.equals(currentLoginUserChannelId)) {
            log.error("用户{}渠道{}没有权限操作{}",UserInfoContextHelper.getCurrentUserId(),currentLoginUserChannelId,channelId);
            throw BusinessException.build("当前登陆用户没有权限操作实体渠道");
        }
    }
}
