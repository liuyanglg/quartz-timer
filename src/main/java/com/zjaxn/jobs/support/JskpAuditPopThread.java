package com.zjaxn.jobs.support;

import com.zjaxn.jobs.service.JskpAuditService;
import com.zjaxn.jobs.service.JskpAuditServiceImpl;
import com.zjaxn.jobs.tasks.JskpAutoAuditTask;
import com.zjaxn.jobs.utils.DateUtil;
import com.zjaxn.jobs.utils.SpringUtil;
import com.zjaxn.jobs.utils.model.JskpCardAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.List;

import static com.zjaxn.jobs.tasks.JskpAutoAuditTask.limitTime;

public class JskpAuditPopThread extends Thread {

    @Autowired
    @Qualifier("jskpAuditService")
    private JskpAuditService jskpAuditService;

    private String threadName;

    private long startTime;

    public JskpAuditPopThread(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public void run() {
        threadName = Thread.currentThread().getName();
        System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " audit start.......");
        try {
            task();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " audit end.......");
            JskpAutoAuditTask.countDown();
        }
    }

    public void task() throws Exception {
        int batchSize = 50;

        if (jskpAuditService == null) {
            jskpAuditService = (JskpAuditService) SpringUtil.getBean("jskpAuditService");
        }
        if (jskpAuditService == null) {
            jskpAuditService = new JskpAuditServiceImpl();
        }

        boolean exit = false;

        while (!exit) {
            if (System.currentTimeMillis() - startTime > limitTime) {
                System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " 超出限制时长，停止审核！");
                break;
            }

            List<JskpCardAudit> list = jskpAuditService.popRedis(batchSize);

            if (list != null) {
                System.out.println("审核: " + list.size());
                jskpAuditService.auditData(list);
            } else {
                exit = true;
            }
        }
    }
}
