package one.yiran.dashboard.captcha.util;

import com.alibaba.fastjson.JSON;
import one.yiran.common.util.Base64Util;
import one.yiran.common.util.RandomUtil;
import one.yiran.dashboard.captcha.model.common.CaptchaBaseMapEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ImageUtils {
    private static Logger logger = LoggerFactory.getLogger(ImageUtils.class);
    private static Map<String, String> originalCacheMap = new ConcurrentHashMap();  //滑块底图
    private static Map<String, String> slidingBlockCacheMap = new ConcurrentHashMap(); //滑块
    private static Map<String, String> picClickCacheMap = new ConcurrentHashMap(); //点选文字
    private static Map<String, String[]> fileNameMap = new ConcurrentHashMap<>();

    public static void cacheImage(String captchaOriginalPathJigsaw, String captchaOriginalPathClick) {
        //滑动拼图
//        if (StringUtils.isBlank(captchaOriginalPathJigsaw)) {
            originalCacheMap.putAll(getResourcesImagesFile("images/captcha/jigsaw/original",8));
            slidingBlockCacheMap.putAll(getResourcesImagesFile("images/captcha/jigsaw/slidingBlock",6));
//        }
//        else {
//            originalCacheMap.putAll(getImagesFile(captchaOriginalPathJigsaw + File.separator + "original"));
//            slidingBlockCacheMap.putAll(getResourcesImagesFile(captchaOriginalPathJigsaw + File.separator + "slidingBlock"));
//        }
        //点选文字
//        if (StringUtils.isBlank(captchaOriginalPathClick)) {
            picClickCacheMap.putAll(getResourcesImagesFile("images/captcha/pic-click",8));
//        } else {
//            picClickCacheMap.putAll(getImagesFile(captchaOriginalPathClick));
//        }
        fileNameMap.put(CaptchaBaseMapEnum.ORIGINAL.getCodeValue(), originalCacheMap.keySet().toArray(new String[0]));
        fileNameMap.put(CaptchaBaseMapEnum.SLIDING_BLOCK.getCodeValue(), slidingBlockCacheMap.keySet().toArray(new String[0]));
        fileNameMap.put(CaptchaBaseMapEnum.PIC_CLICK.getCodeValue(), picClickCacheMap.keySet().toArray(new String[0]));
        logger.info("初始化底图:{}", JSON.toJSONString(fileNameMap));
    }

    public static void cacheBootImage(Map<String, String> originalMap, Map<String, String> slidingBlockMap, Map<String, String> picClickMap) {
        originalCacheMap.putAll(originalMap);
        slidingBlockCacheMap.putAll(slidingBlockMap);
        picClickCacheMap.putAll(picClickMap);
        fileNameMap.put(CaptchaBaseMapEnum.ORIGINAL.getCodeValue(), originalCacheMap.keySet().toArray(new String[0]));
        fileNameMap.put(CaptchaBaseMapEnum.SLIDING_BLOCK.getCodeValue(), slidingBlockCacheMap.keySet().toArray(new String[0]));
        fileNameMap.put(CaptchaBaseMapEnum.PIC_CLICK.getCodeValue(), picClickCacheMap.keySet().toArray(new String[0]));
        logger.info("自定义resource底图:{}", JSON.toJSONString(fileNameMap));
    }

    public static BufferedImage getOriginal() {
        String[] strings = fileNameMap.get(CaptchaBaseMapEnum.ORIGINAL.getCodeValue());
        if (null == strings || strings.length == 0) {
            return null;
        }
        Integer randomInt = RandomUtil.getRandomInt(0, strings.length);
        String s = originalCacheMap.get(strings[randomInt]);
        return getBase64StrToImage(s);
    }

    public static String getslidingBlock() {
        String[] strings = fileNameMap.get(CaptchaBaseMapEnum.SLIDING_BLOCK.getCodeValue());
        if (null == strings || strings.length == 0) {
            return null;
        }
        Integer randomInt = RandomUtil.getRandomInt(0, strings.length);
        String s = slidingBlockCacheMap.get(strings[randomInt]);
        return s;
    }

    public static BufferedImage getPicClick() {
        String[] strings = fileNameMap.get(CaptchaBaseMapEnum.PIC_CLICK.getCodeValue());
        if (null == strings || strings.length == 0) {
            return null;
        }
        Integer randomInt = RandomUtil.getRandomInt(0, strings.length);
        String s = picClickCacheMap.get(strings[randomInt]);
        return getBase64StrToImage(s);
    }

    /**
     * 图片转base64 字符串
     *
     * @param templateImage
     * @return
     */
    public static String getImageToBase64Str(BufferedImage templateImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(templateImage, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = baos.toByteArray();

        Base64.Encoder encoder = Base64.getEncoder();

        return encoder.encodeToString(bytes).trim();
    }

    /**
     * base64 字符串转图片
     *
     * @param base64String
     * @return
     */
    public static BufferedImage getBase64StrToImage(String base64String) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] bytes = decoder.decode(base64String);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static Map<String, String> getResourcesImagesFile(String path,int count) {
        //默认提供六张底图
        Map<String, String> imgMap = new HashMap<>();
        ClassLoader classLoader = ImageUtils.class.getClassLoader();
        for (int i = 1; i <= count; i++) {
            InputStream resourceAsStream = classLoader.getResourceAsStream(path.concat("/").concat(String.valueOf(i).concat(".png")));
            byte[] bytes = new byte[0];
            try {
                bytes = FileCopyUtils.copyToByteArray(resourceAsStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(bytes.length == 0) {
                throw new RuntimeException("目录:"+path+",图片"+i+"加载失败");
            }
            String string = Base64Util.encodeToString(bytes);
            String filename = String.valueOf(i).concat(".png");
            imgMap.put(filename, string);
        }
        return imgMap;
    }

    private static Map<String, String> getImagesFile(String path) {
        Map<String, String> imgMap = new HashMap<>();
        File file = new File(path);
        if (!file.exists()) {
            return new HashMap<>();
        }
        File[] files = file.listFiles();
        Arrays.stream(files).forEach(item -> {
            try {
                FileInputStream fileInputStream = new FileInputStream(item);
                byte[] bytes = FileCopyUtils.copyToByteArray(fileInputStream);
                String string = Base64Util.encodeToString(bytes);
                imgMap.put(item.getName(), string);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return imgMap;
    }

}
