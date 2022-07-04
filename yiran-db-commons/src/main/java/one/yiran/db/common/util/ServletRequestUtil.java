package one.yiran.db.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServletRequestUtil {
    public static <T> T getValueFromRequest(HttpServletRequest request, String name, Type type){
        String rtData = request.getParameter(name);
        if(StringUtils.isNotBlank(rtData)) {
            Class clazz = (Class)type;
            if (clazz.getName().equals(Long.class.getName())) {
                return (T) Long.valueOf(rtData);
            } else if (clazz.equals(Integer.class.getName())) {
                return (T) Integer.valueOf(rtData);
            }
            return (T) rtData;
        }
        JSONObject jsonObj = (JSONObject) request.getAttribute("REQ_JSON_OBJ");
        if(jsonObj != null){
            return jsonObj.getObject(name,type);
        }
        String reqMessage = (String) request.getAttribute("REQ_JSON");
        if(reqMessage == null)
            return null;
        jsonObj = JSON.parseObject(reqMessage);
        request.setAttribute("REQ_JSON_OBJ",jsonObj);
        if(jsonObj == null)
            return null;
        if (StringUtils.contains(type.getTypeName(),"[]")) {
            JSONArray jarr = jsonObj.getJSONArray(name);
            if(jarr == null)
                return null;
            return jarr.toJavaObject(type);
        }
        return jsonObj.getObject(name,type);
    }

    public static <T> T getObjectFromRequest(HttpServletRequest request, Type type){
        Map<String,String> reqMap = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            String v = request.getParameter(key);
            reqMap.put(key,v);
        }
        if (reqMap.size() > 0) {
            return JSON.parseObject(JSON.toJSONString(reqMap),type);
        }

        JSONObject jsonObj = (JSONObject) request.getAttribute("REQ_JSON_OBJ");
        if(jsonObj != null){
            return jsonObj.toJavaObject(type);
        }
        String reqMessage = (String) request.getAttribute("REQ_JSON");
        if(reqMessage == null)
            return null;
        jsonObj = JSON.parseObject(reqMessage);
        request.setAttribute("REQ_JSON_OBJ",jsonObj);
        if(jsonObj == null)
            return null;
        return jsonObj.toJavaObject(type);
    }
}
