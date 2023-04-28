package one.yiran.dashboard.cache;

import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.util.SpringUtil;

import java.util.Set;

public interface DashboardCacheService {

    String set(String key, String value);

    String set(String key, String value, long secondsToExpire);

    boolean exists(String key);

    String delete(String key);

    String get(String key);

    Set<String> keys(String pattern);

    String type();

    boolean expire(String key, long secondsToExpire);

    Long increment(String key, long value);

    public static DashboardCacheService getCacheService(){
        if(Global.userLocalCache()) {
            return SpringUtil.getBean(LocalCacheService.class);
        }
        return SpringUtil.getBean(RedisCacheService.class);
    }
}
