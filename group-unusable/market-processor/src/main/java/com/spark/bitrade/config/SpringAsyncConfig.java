package com.spark.bitrade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/***
 * Async线程池配置
 *
 * @author yangch
 * @time 2018.08.14 13:51
 */

@Configuration
public class SpringAsyncConfig {

    //最小的线程数，缺省：1
    @Value("${spring.async.max.size:2000}")
    private int corePoolSize;
    //private int corePoolSize = 2000;
    //最大的线程数，缺省：Integer.MAX_VALUE
    private int maxPoolSize = 25000;
    /**
     * 缺省值为：Integer.MAX_VALUE
     * 当最小的线程数已经被占用满后，新的任务会被放进queue里面，
     * 当这个queue的capacity也被占满之后，pool里面会创建新线程处理这个任务，直到总线程数达到了max size，
     * 这时系统会拒绝这个任务并抛出TaskRejectedException异常（缺省配置的情况下，可以通过rejection-policy来决定如何处理这种情况）
    */
    private int queueCapacity = 2000;

    private String ThreadNamePrefix = "exo-async-";

    /*@Bean
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        //executor.setMaxPoolSize(maxPoolSize);
        //executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(ThreadNamePrefix);

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }*/

    @Bean
    public ExecutorService getExecutorService(){
        return Executors.newCachedThreadPool();
    }


    /**
     * 自定义异步线程池
     * @return
     */
    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(ThreadNamePrefix);
        executor.setCorePoolSize(corePoolSize);
        //executor.setMaxPoolSize(maxPoolSize);
        //executor.setQueueCapacity(queueCapacity);

        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 使用预定义的异常处理类
        // executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return executor;
    }

}
