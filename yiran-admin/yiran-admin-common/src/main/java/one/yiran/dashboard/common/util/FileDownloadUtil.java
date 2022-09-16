package one.yiran.dashboard.common.util;

import lombok.extern.slf4j.Slf4j;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.constants.Global;
import org.apache.commons.lang3.StringUtils;


@Slf4j
public class FileDownloadUtil {

    public static final String getDownLoadUrl(String fileName) {
        String timestamp = System.currentTimeMillis() + "";
        String sign = Global.fileHash(fileName,timestamp);
        String contextPath = Global.getContextPath();
        String exportMapping = Global.getExportMapping();
        exportMapping = StringUtils.removeStart(exportMapping,"/");
        exportMapping = StringUtils.removeEnd(exportMapping,"/");
        return contextPath + "/" + exportMapping + "?fileName=" + fileName + "&timestamp=" + timestamp+ "&sign=" + sign;
    }

    public static final void checkSign(String fileName,String timestamp, String sign) {
        String calSign = Global.fileHash(fileName,timestamp);
        if(!StringUtils.equals(sign,calSign)) {
            log.info("下载文件签名异常,传递签名{},计算签名:{}",sign,calSign);
            throw BusinessException.build("下载文件失败,签名异常");
        }
    }

}
