package nu.nldv.unroar;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class UppackarenConfig {


    /*
    <bean id="taskScheduler"
          class="org.springframework.scheduling.concurrent.ConcurrentTaskScheduler">
    </bean>
     */
    @Bean
    public ConcurrentTaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

    /*
    <bean id="taskExecutor"
          class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
        <property name="corePoolSize" value="5" />
        <property name="maxPoolSize" value="10" />
        <property name="WaitForTasksToCompleteOnShutdown" value="true" />
    </bean>
     */

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskExecutor;
    }
}
