package com.zjaxn.jobs.support;

import com.base.BaseTest;
import org.junit.Test;

public class JskpAuditPopThreadTest extends BaseTest{
    @Test
    public void task() throws Exception {
        JskpAuditPopThread thread = new JskpAuditPopThread();
        thread.start();
        while (true){}
    }

}