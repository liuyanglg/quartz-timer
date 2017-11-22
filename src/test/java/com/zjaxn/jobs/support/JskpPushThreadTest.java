package com.zjaxn.jobs.support;

import com.base.BaseTest;
import org.junit.Test;

public class JskpPushThreadTest extends BaseTest {
    @Test
    public void pushQueryData() throws Exception {
        JskpPushThread jskpPushThread = new JskpPushThread();
        jskpPushThread.run();
    }

    @Test
    public void mergeCardAudit() throws Exception {
    }

    @Test
    public void lpushRedis() throws Exception {
    }

    @Test
    public void getLastOffset() throws Exception {
    }

}