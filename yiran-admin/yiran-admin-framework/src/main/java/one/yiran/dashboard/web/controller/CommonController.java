package one.yiran.dashboard.web.controller;

import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.annotation.AjaxWrapper;
import one.yiran.common.exception.BusinessException;
import one.yiran.dashboard.common.annotation.ApiParam;
import one.yiran.dashboard.common.annotation.RequireUserLogin;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.common.util.FileDownloadUtil;
import one.yiran.dashboard.common.util.FileUploadUtil;
import one.yiran.dashboard.common.util.FileUtil;
import one.yiran.dashboard.common.util.MimeTypeUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用请求处理
 */
@Slf4j
@Controller
public class CommonController {
    /**
     * 通用下载请求，导出的excel文件
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    //@RequireUserLogin
    @RequestMapping("profile/export")
    public void fileDownload(@ApiParam(required = true) String fileName,
                             @ApiParam(required = true) String timestamp,
                             @ApiParam(required = true) String sign,
                             Boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            log.info("下载文件：{},timestamp:{} sign:{} 删除标示：{}",fileName,timestamp,sign,delete);
            FileDownloadUtil.checkSign(fileName,timestamp,sign);

            if (!FileUtil.isValidFilename(fileName)) {
                throw new Exception(String.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = Global.getExportPath() + fileName;

            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition",
                    "attachment;fileName=" + FileUtil.setFileDownloadHeader(request, realFileName));
            FileUtil.writeBytes(filePath, response.getOutputStream());
            if (delete != null && delete) {
                FileUtil.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
            throw BusinessException.build("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求，编辑器用的
     */
    @RequireUserLogin
    @PostMapping("/common/upload")
    @AjaxWrapper
    public Map uploadFile(MultipartFile file, HttpServletRequest request) throws Exception {
        // 上传并返回新文件名称
        String[] image = MimeTypeUtil.IMAGE_EXTENSION;
        String[] media = MimeTypeUtil.MEDIA_EXTENSION;
        String[] both = ArrayUtils.addAll(image, media);
        log.info("config extension allow:{} / {} ", image,media);
        String fileRelatedPath = FileUploadUtil.upload(Global.getUploadPath(), file, both);
        //String url = serverConfig.getRequestUrl() + fileName;
        String contextPath = request.getContextPath();
        String contextPathAvatar = fileRelatedPath;
        if(!StringUtils.startsWith(fileRelatedPath,"http")) {
            contextPathAvatar = contextPath + fileRelatedPath;
        }
        Map ajax = new HashMap();
        ajax.put("fileName", file.getOriginalFilename());
        ajax.put("url", contextPathAvatar);
        ajax.put("url2", fileRelatedPath);
        return ajax;
    }

//    /**
//     * 本地资源通用下载
//     */
//    @GetMapping("/common/download/resource")
//    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
//            throws Exception
//    {
//        // 本地资源路径
//        String localPath = Global.getProfile();
//        // 数据库资源地址
//        String downloadPath = localPath + StringUtil.substringAfter(resource, SystemConstants.RESOURCE_PREFIX);
//        // 下载名称
//        String downloadName = StringUtil.substringAfterLast(downloadPath, "/");
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("multipart/form-data");
//        response.setHeader("Content-Disposition",
//                "attachment;fileName=" + FileUtil.setFileDownloadHeader(request, downloadName));
//        FileUtil.writeBytes(downloadPath, response.getOutputStream());
//    }
}
