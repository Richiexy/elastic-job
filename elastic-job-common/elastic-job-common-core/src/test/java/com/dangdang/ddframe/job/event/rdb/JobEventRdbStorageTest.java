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

package com.dangdang.ddframe.job.event.rdb;

import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent.ExecutionSource;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent.Source;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent.State;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class JobEventRdbStorageTest {
    
    private JobEventRdbStorage storage;
    
    @Before
    public void setup() throws SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        storage = new JobEventRdbStorage(dataSource);
    }
    
    @Test
    public void assertAddJobExecutionEvent() throws SQLException {
        assertTrue(storage.addJobExecutionEvent(new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0)));
    }
    
    @Test
    public void assertAddJobStatusTraceEvent() throws SQLException {
        assertTrue(storage.addJobStatusTraceEvent(new JobStatusTraceEvent("test_job", "fake_task_id", "fake_slave_id", ExecutionType.READY, "0", 
                Source.LITE_EXECUTOR, State.TASK_RUNNING, "message is empty.")));
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenSuccess() throws SQLException {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        jobExecutionEvent.executionSuccess();
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        assertNotNull(jobExecutionEvent.getCompleteTime());
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenFailure() throws SQLException {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        jobExecutionEvent.executionFailure(new RuntimeException("failure"));
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        assertThat(jobExecutionEvent.getFailureCause(), startsWith("java.lang.RuntimeException: failure"));
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenFailureAndMessageExceed() throws SQLException {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        StringBuilder failureMsg = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            failureMsg.append(i);
        }
        jobExecutionEvent.executionFailure(new RuntimeException("failure" + failureMsg.toString()));
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        assertThat(jobExecutionEvent.getFailureCause(), startsWith("java.lang.RuntimeException: failure"));
    }
    
    @Test
    public void assertFindJobExecutionEvent() throws SQLException {
        storage.addJobExecutionEvent(new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0));
    }
    
}
