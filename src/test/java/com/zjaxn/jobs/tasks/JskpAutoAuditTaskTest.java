package com.zjaxn.jobs.tasks;

import com.base.BaseTest;
import org.junit.Test;

public class JskpAutoAuditTaskTest extends BaseTest {
    @Test
    public void autoAuditCard() throws Exception {
        JskpAutoAuditTask jskpAutoAuditTask = new JskpAutoAuditTask();
        jskpAutoAuditTask.batchAutoAudit();

    }
}