package com.zjaxn.jobs.support;

import com.base.BaseTest;
import org.junit.Test;

public class JskpAuditPushThreadTest extends BaseTest{
    @Test
    public void task() throws Exception {
        for(int i=0;i<30;i++){
//            JskpAuditPushThread thread = new JskpAuditPushThread();
//            thread.start();
            Thread.sleep(2000);
        }
        while (true){}
    }

}