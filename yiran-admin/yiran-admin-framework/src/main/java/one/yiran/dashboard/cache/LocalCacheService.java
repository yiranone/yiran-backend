package one.yiran.dashboard.cache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.thread.NamedThreadFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "dashboard.cache",havingValue = "local")
public class LocalCacheService implements DashboardCacheService{

    private static final ScheduledExecutorService localCacheCleanService = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("local-cache-clean", true));

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws Exception {
        localCacheCleanService.scheduleAtFixedRate(this::cleanUpExpired,1,1, TimeUnit.MINUTES);
    }

    @Override
    public String set(String key, String value) {
        return set(key, value, defaultTtlTime);
    }

    @Override
    public String set(String key, String value, long secondsToExpire) {
        if (secondsToExpire == 0) {
            return null;
        } else if (secondsToExpire < 0) {
            secondsToExpire = -1L;
        } else {
            secondsToExpire = System.currentTimeMillis()/1000 + secondsToExpire;
        }
        if (cacheMap.containsKey(key)) {
            delete(key);
        } else if (cacheList.size() >= maxCapacity) {
            if (cacheList.size() >= maxCapacity) {
                synchronized (this) {
                    cacheMap.remove(cacheList.removeFirst());
                }
            }
        }
        CacheValue<String> cacheValue = new CacheValue<>(value, secondsToExpire);
        synchronized (this) {
            cacheList.addLast(key);
            cacheMap.put(key, cacheValue);
        }
        return value;
    }

    @Override
    public boolean exists(String key) {
        CacheValue<String> cacheValue = cacheMap.get(key);
        if (cacheValue != null) {
            if (cacheValue.ttlTime == -1 || cacheValue.ttlTime > System.currentTimeMillis()/1000) {
                return true;
            }
            delete(key);
            return false;
        }
        return false;
    }

    @Override
    public synchronized String delete(String key) {
        CacheValue<String> remove = cacheMap.remove(key);
        if (remove != null) {
            cacheList.remove(key);
            return remove.value;
        }
        return null;
    }

    @Override
    public boolean expire(String key, long secondsToExpire) {
        long now = System.currentTimeMillis()/1000;
        if (secondsToExpire < 0) {
            secondsToExpire = -1L;
        } else {
            secondsToExpire = now + secondsToExpire;
        }
        CacheValue<String> cacheValue = cacheMap.get(key);
        if (cacheValue != null) {
            if (cacheValue.ttlTime == -1 || cacheValue.ttlTime > now) {
                cacheValue.ttlTime = secondsToExpire;
                return true;
            }
            delete(key);
            return false;
        }
        return false;
    }

    @Override
    public Long increment(String key, long value) {
        String ex = get(key);
        long exv = ex == null ? 0 : Long.valueOf(ex).longValue();
        Long ret = exv + value;
        set(key,ret+"",300);
        return ret;
    }


    @Override
    public String get(String key) {
        CacheValue<String> cacheValue = cacheMap.get(key);
        if (cacheValue != null) {
            if (cacheValue.ttlTime == -1 || cacheValue.ttlTime > System.currentTimeMillis()/1000) {
                cacheList.remove(key);
                cacheList.addLast(key);
                return cacheValue.value;
            } else {
                cacheList.remove(key);
                cacheMap.remove(key);
            }
        }
        return null;
    }

    @Override
    public String type() {
        return "local";
    }

    /**
     * 缓存默认最大容量
     */
    private static final int DEFAULT_MAX_CAPACITY = 1024 * 100;

    /**
     * 最大容量
     */
    private int maxCapacity;

    /**
     * 默认过期时间(毫秒), -1 表示不过期.
     */
    private long defaultTtlTime = -1;

    /**
     * 缓存使用频率
     */
    private LinkedList<String> cacheList = new LinkedList<>();

    /**
     * 缓存对象
     */
    private ConcurrentHashMap<String, CacheValue<String>> cacheMap;


    public LocalCacheService() {
        this(DEFAULT_MAX_CAPACITY);
    }

    /**
     * @param maxCapacity 最大容量
     */
    public LocalCacheService(int maxCapacity) {
        this(maxCapacity, -1L);
    }


    /**
     * @param maxCapacity    最大容量
     * @param defaultTtlTime 默认过期时间(毫秒), -1 表示不过期.
     */
    public LocalCacheService(int maxCapacity, long defaultTtlTime) {
        this.maxCapacity = maxCapacity;
        if (defaultTtlTime > 0) {
            this.defaultTtlTime = defaultTtlTime;
        }
        cacheMap = new ConcurrentHashMap<>();
    }

    /**
     * 定期全面清理过期数据.
     */
    private void cleanUpExpired() {
        long now = System.currentTimeMillis()/1000;
        for (Map.Entry<String, CacheValue<String>> entry : cacheMap.entrySet()) {
            CacheValue<String> value = entry.getValue();
            if (value.ttlTime != -1 && value.ttlTime <= now) {
                synchronized (this) {
                    log.info("缓存删除过期数据 {}",entry.getKey());
                    cacheMap.remove(entry.getKey());
                    cacheList.remove(entry.getKey());
                }
            }
        }
    }


    @AllArgsConstructor
    static class CacheValue<V> {

        /**
         * 缓存对象
         */
        private V value;

        /**
         * 缓存过期时间
         */
        private long ttlTime;
    }


    @AllArgsConstructor
    static class CacheEntry<K, V> implements Map.Entry<K, V> {
        private K k;
        private V v;

        @Override
        public K getKey() {
            return k;
        }

        @Override
        public V getValue() {
            return v;
        }

        @Override
        public V setValue(V value) {
            return this.v = value;
        }
    }
}
