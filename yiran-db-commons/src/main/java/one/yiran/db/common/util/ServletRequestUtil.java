package one.yiran.db.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;

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
        return jsonObj.getObject(name,type);
    }
}
