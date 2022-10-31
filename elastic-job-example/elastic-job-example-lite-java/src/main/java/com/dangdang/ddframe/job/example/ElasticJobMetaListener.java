package com.dangdang.ddframe.job.example;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.internal.election.LeaderService;

/**
 * @author david
 * @since 2022/10/24
 */
public class ElasticJobMetaListener implements ElasticJobListener {
    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        LeaderService leaderService = JobUtils.get(shardingContexts.getJobName());
        System.out.println(leaderService.isLeader());
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        LeaderService leaderService = JobUtils.get(shardingContexts.getJobName());
        System.out.println(leaderService.isLeader());
    }
}
