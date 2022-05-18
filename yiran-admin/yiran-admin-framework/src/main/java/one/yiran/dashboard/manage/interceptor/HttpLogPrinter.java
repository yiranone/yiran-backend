package one.yiran.dashboard.manage.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import one.yiran.common.util.IpUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpLogPrinter {

    public static void print(HttpServletRequest httpServletRequest, Object responseObject) {
        try {
            String reqMessage = httpServletRequest.getAttribute("REQ_JSON") == null ? "" : httpServletRequest.getAttribute("REQ_JSON").toString();

            String respMessage = JSON.toJSONString(responseObject);
            String ip = IpUtil.getRemoteAddr(httpServletRequest);

            StringBuilder sb = new StringBuilder();
            String afterReqMessage = "";
            try {
                if(StringUtils.isNotBlank(reqMessage)) {
                    Map<String, Object> reqMap = JSONObject.parseObject(reqMessage, Map.class);
                    Map<String, Object> newMap = new HashMap<>();
                    reqMap.entrySet().forEach(
                            (ee) -> {
                                if (ee.getKey().contains("pass")) {
                                    newMap.put(ee.getKey(), "***");
                                } else {
                                    newMap.put(ee.getKey(), ee.getValue());
                                }
                            }
                    );
                    afterReqMessage = JSON.toJSONString(newMap);
                }
            } catch (Exception e){

            }
            respMessage = StringUtils.substring(respMessage,0,1000);

            String queryString = StringUtils.defaultIfBlank(httpServletRequest.getQueryString(),"");
            queryString = StringUtils.isNotBlank(queryString) ? "?" + queryString : "";
            sb.append("\n>>>>> 请求:" + ip + " " + httpServletRequest.getMethod()+ " " + httpServletRequest.getRequestURI()
                    + queryString + " ").append("\n");
            if(StringUtils.isNotBlank(afterReqMessage) && !StringUtils.equalsIgnoreCase(afterReqMessage,"{}"))
                sb.append(afterReqMessage).append("\n");
            sb.append("<<<<< 应答:").append("");
            sb.append(respMessage);
            log.info(sb.toString());
        } catch (Exception e){

        }
    }
}
