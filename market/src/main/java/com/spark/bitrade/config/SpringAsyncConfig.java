package com.spark.bitrade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
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
    @Value("${spring.async.min.size:200}")
    private int corePoolSize;
    //private int corePoolSize = 2000;
    //最大的线程数，缺省：Integer.MAX_VALUE
    @Value("${spring.async.max.size:0}")
    private int maxPoolSize = 1000;
    /**
     * 缺省值为：Integer.MAX_VALUE
     * 当最小的线程数已经被占用满后，新的任务会被放进queue里面，
     * 当这个queue的capacity也被占满之后，pool里面会创建新线程处理这个任务，直到总线程数达到了max size，
     * 这时系统会拒绝这个任务并抛出TaskRejectedException异常（缺省配置的情况下，可以通过rejection-policy来决定如何处理这种情况）
     */
    @Value("${spring.async.queue.capacity:0}")
    private int queueCapacity = 10000;

    private String ThreadNamePrefix = "mk-async-";

    //线程存活的时间（5分钟）
    @Value("${spring.async.keep.alive.seconds:3000}")
    private int keepAliveSeconds;

    //线程池配置 交易明细(核心)
    @Value("${spring.async.trade.min.size:5}")
    private int tradePoolSizeMin;
    @Value("${spring.async.trade.max.size:0}")
    private int tradePoolSizeMax;
    @Value("${spring.async.trade.queue.capacity:0}")
    private int tradeQueueCapacity;

    // 订单状态处理
    @Value("${spring.async.order.min.size:2}")
    private int orderPoolSizeMin;
    @Value("${spring.async.order.max.size:0}")
    private int orderPoolSizeMax;
    @Value("${spring.async.order.queue.capacity:0}")
    private int orderQueueCapacity;

    // 返佣
    @Value("${spring.async.reward.min.size:2}")
    private int rewardPoolSizeMin;
    @Value("${spring.async.reward.max.size:0}")
    private int rewardPoolSizeMax;
    @Value("${spring.async.reward.queue.capacity:0}")
    private int rewardQueueCapacity;

    //MongoDB库（交易明细，核心）
    @Value("${spring.async.mongodb.min.size:5}")
    private int mongodbPoolSizeMin;
    @Value("${spring.async.mongodb.max.size:100}")
    private int mongodbPoolSizeMax;
    @Value("${spring.async.mongodb.queue.capacity:200}")
    private int mongodbQueueCapacity;

    //MongoDB库 保存k线、24小时成交额、成交量的处理
    @Value("${spring.async.mongodb2.min.size:5}")
    private int mongodbPoolSizeMin2;
    @Value("${spring.async.mongodb2.max.size:10}")
    private int mongodbPoolSizeMax2;
    @Value("${spring.async.mongodb2.queue.capacity:1000}")
    private int mongodbQueueCapacity2;

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

    /**
     * 初始化线程池
     *
     * @param threadNamePrefix  线程池的前缀名称
     * @param corePoolSize      最小的线程数
     * @param maxPoolSize       最大的线程数
     * @param queueCapacity
     * @param rejectedExecution
     * @return
     */
    public AsyncTaskExecutor taskExecutor(String threadNamePrefix
            , int corePoolSize, int maxPoolSize, int queueCapacity, RejectedExecutionHandler rejectedExecution) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        if (StringUtils.hasText(threadNamePrefix)) {
            executor.setThreadNamePrefix(threadNamePrefix);
        }
        if (corePoolSize > 0) {
            executor.setCorePoolSize(corePoolSize);
        }
        if (maxPoolSize > 0) {
            executor.setMaxPoolSize(maxPoolSize);
        }
        if (queueCapacity > 0) {
            executor.setQueueCapacity(queueCapacity);
        }
        if (keepAliveSeconds > 0) {
            executor.setKeepAliveSeconds(keepAliveSeconds);
        }

        // 设置拒绝策略 rejection-policy：当pool已经达到max size的时候，如何处理新任务
        if (rejectedExecution != null) {
            //不在新线程中执行任务，而是有调用者所在的线程来执行
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        }

        return executor;
    }

    @Bean
    public ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool();
    }


    /**
     * 自定义异步线程池
     *
     * @return
     */
    @Bean
    public AsyncTaskExecutor taskExecutor() {
        return taskExecutor(ThreadNamePrefix, corePoolSize, maxPoolSize
                , queueCapacity, new ThreadPoolExecutor.CallerRunsPolicy());
    }


    //处理 交易明细线程池
    @Bean("trade")
    public AsyncTaskExecutor taskExecutorProcessExchangeTrade() {
        return taskExecutor("trade-", tradePoolSizeMin, tradePoolSizeMax
                , tradeQueueCapacity, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    //处理 返佣（返佣线程池）
    @Bean("reward")
    public AsyncTaskExecutor taskExecutorPromoteReward() {
        return taskExecutor("reward-", rewardPoolSizeMin, rewardPoolSizeMax
                , rewardQueueCapacity, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    //处理 订单状态处理(处理完成订单和撤销订单的线程池)
    @Bean("order")
    public AsyncTaskExecutor taskExecutorOrder() {
        return taskExecutor("order-", orderPoolSizeMin, orderPoolSizeMax
                , orderQueueCapacity, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    //处理 MongoDB库线程池
    @Bean("mongodb")
    public AsyncTaskExecutor taskExecutorMongodb() {
        return taskExecutor("mongodb-", mongodbPoolSizeMin, mongodbPoolSizeMax
                , mongodbQueueCapacity, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    //处理 MongoDB库线程池
    @Bean("mongodb2")
    public AsyncTaskExecutor taskExecutorMongodb2() {
        return taskExecutor("mongodb2-", mongodbPoolSizeMin2, mongodbPoolSizeMax2
                , mongodbQueueCapacity2, new ThreadPoolExecutor.CallerRunsPolicy());
    }

//    private int mongodbMarketPoolSizeMin = 50;//MongoDB库（行情）
//    private int mongodbMarketPoolSizeMax = 100;
//    //处理行情 MongoDB库
//    @Bean("mongodbMarket")
//    public AsyncTaskExecutor taskExecutorMongodbMarket() {
//        return taskExecutor("mongodbMarket-", mongodbMarketPoolSizeMin, mongodbMarketPoolSizeMax
//                , queueCapacity, new ThreadPoolExecutor.CallerRunsPolicy());
//    }
}
