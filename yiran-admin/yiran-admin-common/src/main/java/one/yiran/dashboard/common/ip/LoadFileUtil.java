package one.yiran.dashboard.common.ip;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class LoadFileUtil {
    public static InputStream loadStream(String file) throws Exception {
         return  new ByteArrayInputStream(load(file));
    }
    public static byte[] load(String file) throws Exception {
        //lastModifyTime = qqwryFile.lastModified();
        ByteArrayOutputStream out = null;
        InputStream in = null;
        try {
            out = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            File qqwryFile = new File(file);
            if (qqwryFile.exists()) {
                in = new FileInputStream(qqwryFile);
            } else {
                log.info("查找文件不存在:{},使用CLASSPATH查找" , file);
                String classpathFile = file;
                if(StringUtils.startsWith(file,"/")) {
                    file = StringUtils.substring(file,1);
                }
                if(!file.startsWith("classpath")) {
                    classpathFile ="classpath:" + file;
                }
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources(classpathFile);
                Resource res = resources[0];
                in = res.getInputStream();

                File tmpFile = File.createTempFile(file,"temp");
                tmpFile.deleteOnExit();
                StreamUtils.copy(in,new FileOutputStream(tmpFile));
                in.close();

                in = new FileInputStream(tmpFile);
                if (in == null) {
                    log.info("CLASSPATH查找文件不存在:" + file);
                } else {
                    log.info("CLASSPATH查找文件成功:" + file);
                }
            }
            while(in.read(b) != -1){
                out.write(b);
            }
            byte[] data = out.toByteArray();
            in.close();
            out.close();
            return data;
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
                if(in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("加载文件{}异常",file, e);
            }
            //lock.unlock();
        }
    }
}
