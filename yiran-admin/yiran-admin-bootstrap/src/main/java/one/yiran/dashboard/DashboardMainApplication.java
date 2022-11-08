package one.yiran.dashboard;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


import javax.annotation.PostConstruct;

@ServletComponentScan
@SpringBootApplication(exclude = {JacksonAutoConfiguration.class })
//@EnableCaching
@EnableScheduling
@ComponentScan(value = {"one.yiran","com.biz"})
//@EnableConfigurationProperties(ApplicationProperties.class)
@Slf4j
public class DashboardMainApplication {

    public static void main(String[] args) {
//        System.setProperty("spring.devtools.restart.enabled","false");
        SpringApplication.run(DashboardMainApplication.class, args);
    }

    @PostConstruct
    public void init() {
        //Annotation[] anns = DashboardMainApplication.class.getDeclaredAnnotations();
        //Arrays.stream(anns).forEach(e -> System.out.println(e.annotationType()));
    }

}
