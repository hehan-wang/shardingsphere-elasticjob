/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.api.listener;

import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.internal.guarantee.GuaranteeListenerManager;
import com.dangdang.ddframe.job.lite.internal.guarantee.GuaranteeService;
import com.dangdang.ddframe.job.util.env.TimeService;
import lombok.Setter;

/**
 * 在分布式作业中只执行一次的监听器.
 * 在ElasticJobListener的子类中进行拓展，实现在分布式作业中只执行一次的监听器.
 * 需要{@link GuaranteeListenerManager} 中监听zk变化触发notify.
 * 执行beforeJobExecuted和afterJobExecuted方法可能不是一个实例！！！如果需要统计需要额外的存储
 *
 * @author zhangliang
 */
public abstract class AbstractDistributeOnceElasticJobListener implements ElasticJobListener {

    private final long startedTimeoutMilliseconds;

    private final Object startedWait = new Object();

    private final long completedTimeoutMilliseconds;

    private final Object completedWait = new Object();

    @Setter
    private GuaranteeService guaranteeService;

    private TimeService timeService = new TimeService();

    public AbstractDistributeOnceElasticJobListener(final long startedTimeoutMilliseconds, final long completedTimeoutMilliseconds) {
        //设置开始超时时间在beforeJobExecuted方法wait
        if (startedTimeoutMilliseconds <= 0L) {
            this.startedTimeoutMilliseconds = Long.MAX_VALUE;
        } else {
            this.startedTimeoutMilliseconds = startedTimeoutMilliseconds;
        }
        //设置结束超时时间在afterJobExecuted方法wait
        if (completedTimeoutMilliseconds <= 0L) {
            this.completedTimeoutMilliseconds = Long.MAX_VALUE;
        } else {
            this.completedTimeoutMilliseconds = completedTimeoutMilliseconds;
        }
    }

    @Override
    public final void beforeJobExecuted(final ShardingContexts shardingContexts) {
        //1. 注册分配给该实例的所有分片 /guarantee/started/{item}
        guaranteeService.registerStart(shardingContexts.getShardingItemParameters().keySet());
        //2. 等待所有分片
        //2.1 最后一个分片注册成功后，所有分片都注册成功，开始执行子类逻辑，并清除started节点
        if (guaranteeService.isAllStarted()) {
            doBeforeJobExecutedAtLastStarted(shardingContexts);
            guaranteeService.clearAllStartedInfo();
            return;
        }
        //2.1 如果不是最后一个started,则等待startedTimeoutMilliseconds时间
        long before = timeService.getCurrentMillis();
        try {
            synchronized (startedWait) {
                startedWait.wait(startedTimeoutMilliseconds);
            }
        } catch (final InterruptedException ex) {
            Thread.interrupted();
        }
        //3. 如果等待超时,则抛出异常,并清除started信息
        if (timeService.getCurrentMillis() - before >= startedTimeoutMilliseconds) {
            guaranteeService.clearAllStartedInfo();
            handleTimeout(startedTimeoutMilliseconds);
        }
    }

    // 原理同上
    @Override
    public final void afterJobExecuted(final ShardingContexts shardingContexts) {
        guaranteeService.registerComplete(shardingContexts.getShardingItemParameters().keySet());
        if (guaranteeService.isAllCompleted()) {
            doAfterJobExecutedAtLastCompleted(shardingContexts);
            guaranteeService.clearAllCompletedInfo();
            return;
        }
        long before = timeService.getCurrentMillis();
        try {
            synchronized (completedWait) {
                completedWait.wait(completedTimeoutMilliseconds);
            }
        } catch (final InterruptedException ex) {
            Thread.interrupted();
        }
        if (timeService.getCurrentMillis() - before >= completedTimeoutMilliseconds) {
            guaranteeService.clearAllCompletedInfo();
            handleTimeout(completedTimeoutMilliseconds);
        }
    }

    private void handleTimeout(final long timeoutMilliseconds) {
        throw new JobSystemException("Job timeout. timeout mills is %s.", timeoutMilliseconds);
    }

    /**
     * 分布式环境中最后一个作业执行前的执行的方法.
     *
     * @param shardingContexts 分片上下文
     */
    public abstract void doBeforeJobExecutedAtLastStarted(ShardingContexts shardingContexts);

    /**
     * 分布式环境中最后一个作业执行后的执行的方法.
     *
     * @param shardingContexts 分片上下文
     */
    public abstract void doAfterJobExecutedAtLastCompleted(ShardingContexts shardingContexts);

    /**
     * 通知任务开始.
     */
    public void notifyWaitingTaskStart() {
        synchronized (startedWait) {
            startedWait.notifyAll();
        }
    }

    /**
     * 通知任务结束.
     */
    public void notifyWaitingTaskComplete() {
        synchronized (completedWait) {
            completedWait.notifyAll();
        }
    }
}
