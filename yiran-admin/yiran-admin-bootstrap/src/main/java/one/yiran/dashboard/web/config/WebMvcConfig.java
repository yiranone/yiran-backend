package one.yiran.dashboard.web.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.biz.interceptor.MemberInterceptor;
import one.yiran.dashboard.common.constants.Global;
import one.yiran.dashboard.interceptor.AuthInterceptor;
import one.yiran.dashboard.resolver.ApiChannelParamResolver;
import one.yiran.dashboard.resolver.ApiUserParamResolver;
import one.yiran.dashboard.resolver.ObjectParamTypeParamResolver;
import one.yiran.dashboard.resolver.PageRequestParamResolver;
import one.yiran.dashboard.resolver.SimpleParamTypeParamResolver;
import one.yiran.dashboard.web.config.json.LocalDateFormatSerializer;
import one.yiran.dashboard.web.filter.AjaxMethodReturnValueHandler;
import one.yiran.dashboard.web.filter.MyInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvc
//@EnableAsync
//@Enable
public class WebMvcConfig implements WebMvcConfigurer, InitializingBean {

    static {
        SerializeConfig.global.put(LocalDate.class,new LocalDateFormatSerializer());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//
        registry.addResourceHandler("/profile/avatar/**")
                .addResourceLocations("file:" + Global.getAvatarPath());
        registry.addResourceHandler("/profile/upload/**")
                .addResourceLocations("file:" + Global.getUploadPath());
    }

    @Autowired
    private AuthInterceptor authInterceptor;
    @Autowired
    private MemberInterceptor memberInterceptor;
    //添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        MyInterceptor interceptor = new MyInterceptor();
        registry.addInterceptor(interceptor).addPathPatterns("/**");
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        registry.addInterceptor(memberInterceptor).addPathPatterns("/**");
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
//        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
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
    }


    //添加转换器
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0,ajaxMessageConverter());
    }

    @Bean
    public PageRequestParamResolver pageRequestParamResolver(){
        return new PageRequestParamResolver();
    }

    @Bean
    public SimpleParamTypeParamResolver simpleParamTypeParamResolver(){
        return new SimpleParamTypeParamResolver();
    }

    @Bean
    public ApiChannelParamResolver apiChannelParamResolver(){
        return new ApiChannelParamResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(pageRequestParamResolver());
        argumentResolvers.add(simpleParamTypeParamResolver());
        argumentResolvers.add(new ObjectParamTypeParamResolver());
        argumentResolvers.add(new ApiUserParamResolver());
        argumentResolvers.add(apiChannelParamResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowCredentials(true)
//                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS", "HEAD")
//                .maxAge(3600 * 24);
    }

    private CorsConfiguration corsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 请求常用的三种配置，*代表允许所有，也可以自定义属性（比如 header 只能带什么，只能是 post 方式等）
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig());
        return new CorsFilter(source);
    }


    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcTaskExecutor());
//        configurer.setDefaultTimeout(180_000);
    }

    @Bean
    public ThreadPoolTaskExecutor mvcTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("mvc-task-");
        return taskExecutor;
    }
}