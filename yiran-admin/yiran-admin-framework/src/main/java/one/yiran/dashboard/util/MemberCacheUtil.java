package one.yiran.dashboard.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.model.MemberSession;
import one.yiran.dashboard.common.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class MemberCacheUtil {

    private static final String DEFAULT_CACHE = "DEFAULT";
    private static final String SESSION_PREFIX = ".member.{";
    private static final String SESSION_SUFFIX = "}.token";

    private static final String SMS_PREFIX = ".sms.{";
    private static final String SMS_SUFFIX = "}.code";

    private static final int SESSION_TIMEOUT = 604800; //session单位为s 604800=7天

    public static int getSessionTimeout(){
        return SESSION_TIMEOUT;
    }

    public static String getUserToken(Long userId) {
        return userId + "_" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void setSessionInfo(String key, MemberSession member) {
        String s = JSON.toJSONString(member);
        if(Global.userLocalCache()) {
            Cache cache = getCache();
            cache.put(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX, s);
            return;
        }

        Jedis resource = getJedis();
        try {
            resource.set(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX, s, SetParams.setParams().ex(SESSION_TIMEOUT));
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

    public static MemberSession getSessionInfo(String key) {
        if(Global.userLocalCache()) {
            Cache cache = getCache();
            Object o = cache.getIfPresent(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX);
            if (o == null)
                return null;
            return JSONObject.parseObject(o.toString(), MemberSession.class);
        }
        Jedis pool = getJedis();
        try {
            Object o = pool.get(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX);
            if (o == null)
                return null;
            pool.expire(Global.getRedisPrefix() + SESSION_PREFIX + key + SESSION_SUFFIX, SESSION_TIMEOUT);
            return JSONObject.parseObject(o.toString(), MemberSession.class);
        } finally {
            if (pool != null)
                pool.close();
        }
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
    private static Cache getCache() {
        return SpringUtil.getBean(Cache.class);
    }
}
