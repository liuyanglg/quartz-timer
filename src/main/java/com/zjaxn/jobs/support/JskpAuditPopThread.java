package com.zjaxn.jobs.support;

import com.zjaxn.jobs.service.JskpAuditService;
import com.zjaxn.jobs.service.JskpAuditServiceImpl;
import com.zjaxn.jobs.tasks.JskpAutoAuditTask;
import com.zjaxn.jobs.utils.DateUtil;
import com.zjaxn.jobs.utils.SpringUtil;
import com.zjaxn.jobs.utils.model.JskpCardAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.zjaxn.jobs.tasks.JskpAutoAuditTask.*;

@Component
public class JskpAuditPopThread extends Thread {

    @Autowired
    @Qualifier("jskpAuditService")
    private JskpAuditService jskpAuditService;

    private String threadName;

    @Override
    public void run() {
        threadName = Thread.currentThread().getName();
        System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " start.......");
        task();
        JskpAutoAuditTask.countDown();
        System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " end.......");
    }

    public void task() {
        int batchSize = 100;
        try {
            if (jskpAuditService == null) {
                jskpAuditService = (JskpAuditService) SpringUtil.getBean("jskpAuditService");
            }
            if (jskpAuditService == null) {
                jskpAuditService = new JskpAuditServiceImpl();
            }

            boolean exit = false;
            while (!exit) {
//                if (System.currentTimeMillis() - startTime > limitTime) {
//                    System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + ":超出时间限制！");
//                    JskpAutoAuditTask.countDown();
//                    return;
//                }

                List<JskpCardAudit> list = jskpAuditService.popRedis(batchSize);
                if (list != null) {
                    jskpAuditService.auditData(list);
                }

                if (pushFinish && list == null) {
                    exit = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JskpAutoAuditTask.countDown();
        }
    }
}
