package one.yiran.dashboard.aspect;

import com.google.common.collect.Maps;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

//@Component
public class TimeCostBeanPostProcessor implements BeanPostProcessor {
    private Map<String, Long> costMap = Maps.newConcurrentMap();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        costMap.put(beanName, System.currentTimeMillis());
        return bean;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (costMap.containsKey(beanName)) {
            Long start = costMap.get(beanName);
            long cost  = System.currentTimeMillis() - start;
            if (cost > 0) {
                costMap.put(beanName, cost);
                System.out.println("bean: " + beanName + "\ttime: " + cost);
            }
        }
        return bean;
    }
}