package one.yiran.dashboard.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.cache.DashboardCacheService;
import one.yiran.dashboard.cache.LocalCacheService;
import one.yiran.dashboard.cache.RedisCacheService;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.common.util.SpringUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Slf4j
public class MemberCacheUtil {

    private static final String DEFAULT_CACHE = "DEFAULT";
    private static final String SESSION_PREFIX = ".member.{";
    private static final String SESSION_SUFFIX = "}.token";

    private static final String SMS_PREFIX = ".sms_member.{";
    private static final String SMS_SUFFIX = "}.code";

    private static final String CONFIG_PREFIX = ".config.{";
    private static final String CONFIG_SUFFIX = "}.token";

    private static final int SESSION_TIMEOUT = 86400; //session单位为s 604800=7天 86400=1天

    public static int getSessionTimeout(){
        return SESSION_TIMEOUT;
    }

    public static String getMemberToken(Long memberId) {
        return memberId + "_" + RandomStringUtils.randomAlphanumeric(38);
    }

    public static void setSessionInfo(String key, MemberSession member) {
        //踢掉上一个
        Set<String> keys = getCacheService().keys(Global.getRedisPrefix() + SESSION_PREFIX + member.getMemberId() + "_*");
        if (null != keys && keys.size() > 0) {
            for (String k : keys) {
                log.info("清理上次登录的信息:{}", k);
                clearKey(k);
            }
        }

        String s = JSON.toJSONString(member);
        String k = Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX;
        getCacheService().set(k,s,getSessionTimeout());
    }

    public static void removeSessionInfo(String key) {
        String k = Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX;
        getCacheService().delete(k);
    }

    public static MemberSession getSessionInfo(String key) {
        String k = Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX;
        String data = getCacheService().get(k);
        if (StringUtils.isBlank(data))
            return null;
        getCacheService().expire(k, getSessionTimeout());
        return JSONObject.parseObject(data, MemberSession.class);
    }

    public static MemberSession getSessionInfo(HttpServletRequest request) {
        if(request == null) {
            return null;
        }
        String token = request.getHeader(Global.getAuthKey());
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return getSessionInfo(token);
    }

    public static void setSmsInfo(String key, String value) {
        String k = Global.getRedisPrefix() + SMS_PREFIX + key + SMS_SUFFIX;
        getCacheService().set(k,value,5 * 60);
    }

    public static String getSmsInfo(String key) {
        String k = Global.getRedisPrefix() + SMS_PREFIX + key + SMS_SUFFIX;
        return getCacheService().get(k);
    }

    public static void removeSmsInfo(String key) {
        String k = Global.getRedisPrefix() + SMS_PREFIX + key + SMS_SUFFIX;
        getCacheService().delete(k);
    }

    public static void removeSystemConfig(String key) {
        String k = Global.getRedisPrefix() + CONFIG_PREFIX + key + CONFIG_SUFFIX;
        getCacheService().delete(k);
    }

    public static String getSystemConfig(Long channelId, String key) {
        String result = getSystemConfig(channelId + "_" + key);
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        return getSystemConfig(key);
    }

    public static String getSystemConfig(String key) {
        String k = Global.getRedisPrefix() + CONFIG_PREFIX + key + CONFIG_SUFFIX;
        return getCacheService().get(k);
    }

    public static void setSystemConfig(String key, String value) {
        String k = Global.getRedisPrefix() + CONFIG_PREFIX + key + CONFIG_SUFFIX;
        getCacheService().set(k,value);
    }

    public static void setSystemConfig(String key, String value, int ttlSeconds) {
        String k = Global.getRedisPrefix() + CONFIG_PREFIX + key + CONFIG_SUFFIX;
        getCacheService().set(k,value,ttlSeconds);

    }

    public static void clearKey(String key) {
        try {
            getCacheService().delete(key);
        } catch (Exception e) {
            log.error("access cache error", e);
        }
    }

    private static DashboardCacheService getCacheService(){
        if(Global.userLocalCache()) {
            return SpringUtil.getBean(LocalCacheService.class);
        }
        return SpringUtil.getBean(RedisCacheService.class);
    }
}
