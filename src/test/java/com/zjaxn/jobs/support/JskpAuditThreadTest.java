package com.zjaxn.jobs.support;

import com.base.BaseTest;
import org.junit.Test;

public class JskpAuditThreadTest  extends BaseTest{
    @Test
    public void lpopRedis() throws Exception {
        JskpAuditThread thread = new JskpAuditThread();
        thread.batchAutoAudit();
    }

    @Test
    public void getJedis() throws Exception {
    }

}