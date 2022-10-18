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

package com.dangdang.ddframe.job.example;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.example.job.dataflow.JavaDataflowJob;
import com.dangdang.ddframe.job.example.job.simple.JavaSimpleJob;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.commons.dbcp.BasicDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

public final class MyJavaMain {

    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";

    private static final String JOB_NAMESPACE = "elastic-job-example-lite-java";

    // switch to MySQL by yourself
    private static final String EVENT_RDB_STORAGE_DRIVER = "com.mysql.jdbc.Driver";
    private static final String EVENT_RDB_STORAGE_URL = "jdbc:mysql://localhost:3306/elastic_job_log";

//    private static final String EVENT_RDB_STORAGE_DRIVER = "org.h2.Driver";

//    private static final String EVENT_RDB_STORAGE_URL = "jdbc:h2:mem:job_event_storage";

    private static final String EVENT_RDB_STORAGE_USERNAME = "root";

    private static final String EVENT_RDB_STORAGE_PASSWORD = "root";

    //TODO 启动入口
    public static void main(final String[] args) throws IOException {
        //配置注册中心
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZOOKEEPER_CONNECTION_STRING, JOB_NAMESPACE);
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
        regCenter.init();

        //配置任务日志数据源
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(EVENT_RDB_STORAGE_DRIVER);
        result.setUrl(EVENT_RDB_STORAGE_URL);
        result.setUsername(EVENT_RDB_STORAGE_USERNAME);
        result.setPassword(EVENT_RDB_STORAGE_PASSWORD);
        JobEventConfiguration jobEventConfig = new JobEventRdbConfiguration(result);

        //配置任务
        JobCoreConfiguration coreConfig = JobCoreConfiguration.newBuilder("javaSimpleJob", "0/10 * * * * ?", 3)
                .failover(true)
                .misfire(false)
                .shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").build();
        SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(coreConfig, JavaSimpleJob.class.getCanonicalName());
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(simpleJobConfig)
                .overwrite(true).build();
        //初始化任务
        new JobScheduler(regCenter, liteJobConfiguration, jobEventConfig).init();
    }
}
