package one.yiran.dashboard.web.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.File;

@Configuration
public class WebServerConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
//        tomcat.addInitializers(new ServletContextInitializer(){
//            @Override
//            public void onStartup(ServletContext servletContext) throws ServletException {
//                XmlWebApplicationContext context = new XmlWebApplicationContext();
//                //AnnotationConfigWebApplicationContext context = new XmlWebApplicationContext();
//                //context.setConfigLocations(new String[]{"/WEB-INF/springmvc-servlet.xml"});
//                //context.setConfigLocations(new String[]{"classpath:springmvc-servlet.xml"});
//                ServletRegistration.Dynamic dispatcher = servletContext
//                        .addServlet("springmvc", new DispatcherServlet(context));
//
//                dispatcher.setLoadOnStartup(1);
//                dispatcher.addMapping("/*");
//            }
//        });
        //tomcat.addContextValves();
        //tomcat.setPort(port);

        return tomcat;
    }
}
