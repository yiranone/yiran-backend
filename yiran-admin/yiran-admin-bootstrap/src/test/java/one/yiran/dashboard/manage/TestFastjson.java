package one.yiran.dashboard.manage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import lombok.Data;
import one.yiran.dashboard.web.config.json.LocalDateFormatSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestFastjson {

    @Data
    static class DataWrap {
        private LocalDate s1 = LocalDate.now();
        @JSONField(format = "yyyy-MM-dd")
        private LocalDate s2= LocalDate.now();

        private LocalDateTime dt1 = LocalDateTime.now();
        @JSONField(format = "yyyy-MM-dd HH:mm:ss SSS")
        private LocalDateTime dt2 = LocalDateTime.now();
    }

    public static void main(String[] args) throws IOException {

        SerializeConfig serializeConfig = new SerializeConfig();
        serializeConfig.put(LocalDate.class, new LocalDateFormatSerializer());
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(SerializerFeature.WriteDateUseDateFormat);
        config.setSerializeConfig(serializeConfig);

        SerializeConfig.global.put(LocalDate.class,new LocalDateFormatSerializer());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JSON.writeJSONString(
                baos, new DataWrap(),
                config.getSerializeFilters(),
                config.getSerializerFeatures()
        );
        System.out.println(baos);
        System.out.println(JSON.toJSONString(new DataWrap()));
        System.out.println(com.alibaba.fastjson2.JSON.toJSONString(new DataWrap()));
    }
}
