package one.yiran.dashboard.web.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.manage.interceptor.AuthInterceptor;
import one.yiran.dashboard.resolver.ApiUserParamResolver;
import one.yiran.dashboard.resolver.ObjectParamTypeParamResolver;
import one.yiran.dashboard.resolver.PageRequestParamResolver;
import one.yiran.dashboard.resolver.SimpleParamTypeParamResolver;
import one.yiran.dashboard.web.filter.AjaxMethodReturnValueHandler;
import one.yiran.dashboard.web.filter.MyInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
//@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer, InitializingBean {

//    @Autowired
    //private ObjectMapper objectMapper;

    private static SerializeConfig serializeConfig = new SerializeConfig();
    private static String dateFormat;

    static {
        dateFormat = "yyyy-MM-dd HH:mm:ss";
        serializeConfig.put(Date.class, new SimpleDateFormatSerializer(dateFormat));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//
//        registry.addResourceHandler("/profile/avatar/**")
//                .addResourceLocations("file:" + Global.getAvatarPath());
        registry.addResourceHandler("/profile/upload/**")
                .addResourceLocations("file:" + Global.getUploadPath());
    }

    @Autowired
    private AuthInterceptor authInterceptor;
    //添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        MyInterceptor interceptor = new MyInterceptor();
        registry.addInterceptor(interceptor).addPathPatterns("/**");
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //registry.addViewController("/login").setViewName("login");
        //registry.addViewController("/").setViewName("forward:" + "/index");
        //registry.addViewController("/login").setViewName("login2");
    }

    @Bean
    public FastJsonHttpMessageConverter ajaxMessageConverter() {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        //自定义配置...
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(SerializerFeature.QuoteFieldNames,
                SerializerFeature.WriteEnumUsingToString,
                /*SerializerFeature.WriteMapNullValue,*/
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.WriteBigDecimalAsPlain,
                SerializerFeature.DisableCircularReferenceDetect);
        config.setSerializeConfig(serializeConfig);

        fastJsonHttpMessageConverter.setFastJsonConfig(config);


        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_PDF);
        supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        supportedMediaTypes.add(MediaType.IMAGE_GIF);
        supportedMediaTypes.add(MediaType.IMAGE_JPEG);
        supportedMediaTypes.add(MediaType.IMAGE_PNG);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.TEXT_XML);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);

        return fastJsonHttpMessageConverter;
    }

    //
    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    //@Autowired
    //private ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver;

    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> handlers = requestMappingHandlerAdapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> newHandels = new ArrayList<>(handlers);
        List<HttpMessageConverter<?>> converts = new ArrayList<>();
        converts.add(ajaxMessageConverter());
        newHandels.add(0, new AjaxMethodReturnValueHandler(converts));

        requestMappingHandlerAdapter.setReturnValueHandlers(newHandels);
//        System.out.println(newHandels);
//        HandlerMethodReturnValueHandlerComposite composite = exceptionHandlerExceptionResolver.getReturnValueHandlers();
//        composite.addHandler(new AjaxMethodReturnValueHandler(converts));

    }


    //添加转换器
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //converters.add(1,ajaxMessageConverter());

    }

    @Bean
    public PageRequestParamResolver pageRequestParamResolver(){
        return new PageRequestParamResolver();
    }

    @Bean
    public SimpleParamTypeParamResolver simpleParamTypeParamResolver(){
        return new SimpleParamTypeParamResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(pageRequestParamResolver());
        argumentResolvers.add(simpleParamTypeParamResolver());
        argumentResolvers.add(new ObjectParamTypeParamResolver());
        argumentResolvers.add(new ApiUserParamResolver());
    }


//    @Bean
//    @ConditionalOnMissingBean
//    public InternalResourceViewResolver defaultViewResolver() {
//        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//        resolver.setPrefix("templates/");
//        resolver.setSuffix(".html");
//        return resolver;
//    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowCredentials(true)
//                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS", "HEAD")
//                .maxAge(3600 * 24);
//    }
}