package one.yiran.dashboard.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import one.yiran.dashboard.common.model.UserSession;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class UserCacheUtil {

    private static final String DEFAULT_CACHE = "DEFAULT";
    private static final String SESSION_PREFIX = ".user.{";
    private static final String SESSION_SUFFIX = "}.token";

    private static final String SMS_PREFIX = ".sms.{";
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
        if(Global.userLocalCache()) {
            Cache cache = getCache();
            cache.put(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX, s);
            return;
        }

        Jedis resource = getJedis();
        try {
            resource.set(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX, s, SetParams.setParams().ex(getSessionTimeout()));
        } finally {
            if (resource != null)
                resource.close();
        }
    }

    public static void removeSessionInfo(String key) {
        if(Global.userLocalCache()) {
            Cache cache = getCache();
            cache.invalidate(key);
            return;
        }
        Jedis pool = getJedis();
        try {
            pool.del(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX);
        } finally {
            if (pool != null)
                pool.close();
        }
    }

    public static UserSession getSessionInfo(String key) {
        if(Global.userLocalCache()) {
            Cache cache = getCache();
            Object o = cache.getIfPresent(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX);
            if (o == null)
                return null;
            return JSONObject.parseObject(o.toString(), UserSession.class);
        }
        Jedis pool = getJedis();
        try {
            Object o = pool.get(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX);
            if (o == null)
                return null;
            pool.expire(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX, getSessionTimeout());
            return JSONObject.parseObject(o.toString(), UserSession.class);
        } finally {
            if (pool != null)
                pool.close();
        }
    }

    public static UserSession getSessionInfo(HttpServletRequest request) {
        String token = request.getHeader(Global.getAuthKey());
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return getSessionInfo(token);
    }


    public static void setSmsInfo(String key, String value) {
        if(Global.userLocalCache()) {
            return;
        }
        Jedis pool = getJedis();
        try {
            pool.set(Global.getRedisPrefix() + SMS_PREFIX + key + SMS_SUFFIX, value, SetParams.setParams().ex(300));
        } finally {
            if (pool != null)
                pool.close();
        }
    }

    public static String getSmsInfo(String key) {
        if(Global.userLocalCache()) {
            return "";
        }
        Jedis pool = getJedis();
        try {
            Object o = pool.get(Global.getRedisPrefix() + SMS_PREFIX + key + SMS_SUFFIX);
            return o == null ? null : o.toString();
        } finally {
            if (pool != null)
                pool.close();
        }
    }

    private static Jedis getJedis() {
        JedisPool jedisPool = SpringUtil.getBean(JedisPool.class);
        return jedisPool.getResource();
    }
    private static com.google.common.cache.Cache getCache() {
        return SpringUtil.getBean(com.google.common.cache.Cache.class);
    }
}
