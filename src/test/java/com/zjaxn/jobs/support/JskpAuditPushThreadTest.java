package com.zjaxn.jobs.support;

import com.base.BaseTest;
import org.junit.Test;

public class JskpAuditPushThreadTest extends BaseTest{
    @Test
    public void task() throws Exception {
        JskpAuditPushThread thread = new JskpAuditPushThread();
        thread.start();
        while (true){}
    }

}