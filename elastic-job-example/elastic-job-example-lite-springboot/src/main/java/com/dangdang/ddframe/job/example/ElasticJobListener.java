package com.dangdang.ddframe.job.example;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.springframework.stereotype.Component;

/**
 * @author david
 * @since 2022/9/12
 */
@Component
public class ElasticJobListener extends AbstractDistributeOnceElasticJobListener {

    public ElasticJobListener() {
        super(0, 0);
    }


    @Override
    public void doBeforeJobExecutedAtLastStarted(ShardingContexts shardingContexts) {
        System.out.println("before:" + shardingContexts);
    }

    @Override
    public void doAfterJobExecutedAtLastCompleted(ShardingContexts shardingContexts) {
        System.out.println("after:" + shardingContexts);
    }

}