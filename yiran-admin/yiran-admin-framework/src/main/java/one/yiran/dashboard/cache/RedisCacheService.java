package one.yiran.dashboard.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Set;

@Service
@ConditionalOnProperty(name = "dashboard.cache",havingValue = "redis")
public class RedisCacheService implements DashboardCacheService{

    @Autowired
    private JedisPool pool;

    @Override
    public String set(String key, String value) {
        return set(key,value,-1);
    }

    @Override
    public String set(String key, String value, long secondsToExpire) {
        Jedis resource = pool.getResource();
        try {
            if(secondsToExpire < 0) {
                return resource.set(key, value);
            } else {
                return resource.set(key, value, SetParams.setParams().px(secondsToExpire*1000));
            }
        } finally {
            if (resource != null) {
                resource.close();
            }
        }
    }

    @Override
    public boolean exists(String key) {
        Jedis resource = pool.getResource();
        try {
            return resource.exists(key);
        } finally {
            if (resource != null) {
                resource.close();
            }
        }
    }

    @Override
    public String delete(String key) {
        Jedis resource = pool.getResource();
        try {
            String v = get(key);
            resource.del(key);
            return v;
        } finally {
            if (resource != null) {
                resource.close();
            }
        }
    }

    @Override
    public String get(String key) {
        Jedis resource = pool.getResource();
        try {
            return resource.get(key);
        } finally {
            if (resource != null) {
                resource.close();
            }
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        Jedis resource = pool.getResource();
        try {
            return resource.keys(pattern);
        } finally {
            if (resource != null) {
                resource.close();
            }
        }
    }

    @Override
    public String type() {
        return "redis";
    }

    @Override
    public boolean expire(String key, long secondsToExpire) {
        Jedis resource = pool.getResource();
        try {
            resource.pexpire(key,secondsToExpire*1000);
            return true;
        } finally {
            if (resource != null) {
                resource.close();
            }
        }
    }

    @Override
    public Long increment(String key, long value) {
        Jedis resource = pool.getResource();
        try {
            return resource.incrBy(key,value);
        } finally {
            if (resource != null) {
                resource.close();
            }
        }
    }
}
