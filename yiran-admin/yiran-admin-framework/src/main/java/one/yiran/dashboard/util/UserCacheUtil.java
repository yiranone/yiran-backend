package one.yiran.dashboard.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.yiran.dashboard.cache.DashboardCacheService;
import one.yiran.dashboard.cache.LocalCacheService;
import one.yiran.dashboard.cache.RedisCacheService;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.common.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class UserCacheUtil {

    private static final String DEFAULT_CACHE = "DEFAULT";
    private static final String SESSION_PREFIX = ".user.{";
    private static final String SESSION_SUFFIX = "}.token";

    private static final String SMS_PREFIX = ".sms_user.{";
    private static final String SMS_SUFFIX = "}.code";

    // 返回s
    public static int getSessionTimeout(){
        return Global.getSessionTimeout().intValue() * 60;
    }

    public static String getUserToken(Long userId) {
        return userId + "_" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void setSessionInfo(String key, UserSession sessionInfo) {
        String s = JSON.toJSONString(sessionInfo);
        String k = Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX;
        getCacheService().set(k,s,getSessionTimeout());
    }

    public static void removeSessionInfo(String key) {
        String k = Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX;
        getCacheService().delete(k);
    }

    public static UserSession getSessionInfo(String key) {
        String k = Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX;
        String data = getCacheService().get(k);
        if (StringUtils.isBlank(data))
            return null;
        getCacheService().expire(k, getSessionTimeout());
        return JSONObject.parseObject(data, UserSession.class);
    }

    public static UserSession getSessionInfo(HttpServletRequest request) {
        String token = request.getHeader(Global.getAuthKey());
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return getSessionInfo(token);
    }


    public static void setSmsInfo(String key, String value) {
        String k = Global.getRedisPrefix() + SMS_PREFIX + key + SMS_SUFFIX;
        getCacheService().set(k,value,5*60*1000);
    }

    public static String getSmsInfo(String key) {
        String k = Global.getRedisPrefix() + SMS_PREFIX + key + SMS_SUFFIX;
        return getCacheService().get(k);
    }

    private static DashboardCacheService getCacheService(){
        if(Global.userLocalCache()) {
            return SpringUtil.getBean(LocalCacheService.class);
        }
        return SpringUtil.getBean(RedisCacheService.class);
    }

//    private static Jedis getJedis() {
//        JedisPool jedisPool = SpringUtil.getBean(JedisPool.class);
//        return jedisPool.getResource();
//    }
//    private static com.google.common.cache.Cache getCache() {
//        return SpringUtil.getBean(com.google.common.cache.Cache.class);
//    }
}
