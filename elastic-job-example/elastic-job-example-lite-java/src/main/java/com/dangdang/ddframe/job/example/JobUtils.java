package com.dangdang.ddframe.job.example;

import com.dangdang.ddframe.job.lite.internal.election.LeaderService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author david
 * @since 2022/10/27
 */
public class JobUtils {
    public static final Map<String, LeaderService> leaderServiceMap = new ConcurrentHashMap<>();

    public static void reg(final CoordinatorRegistryCenter regCenter, final String jobName) {
        LeaderService leaderService = new LeaderService(regCenter, jobName);
        leaderServiceMap.put(jobName, leaderService);
    }

    public static LeaderService get(String jobName) {
        return leaderServiceMap.get(jobName);
    }
}
