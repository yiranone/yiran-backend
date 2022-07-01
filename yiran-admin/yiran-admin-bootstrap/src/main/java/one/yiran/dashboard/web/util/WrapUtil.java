package one.yiran.dashboard.web.util;

import java.util.HashMap;
import java.util.Map;

public class WrapUtil {
    public static Map<String,Object> wrap(String key, Object value){
        return new HashMap<String,Object>(){{
            put(key,value);
        }};
    }

    public static Map<String,Object> wrapWithExist(boolean b){
        return wrap("exist",b);
    }
}
