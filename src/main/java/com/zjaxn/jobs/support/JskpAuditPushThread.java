package com.zjaxn.jobs.support;

import com.zjaxn.jobs.service.JskpAuditService;
import com.zjaxn.jobs.service.JskpAuditServiceImpl;
import com.zjaxn.jobs.utils.DateUtil;
import com.zjaxn.jobs.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.zjaxn.jobs.tasks.JskpAutoAuditTask.*;

@Component
public class JskpAuditPushThread extends Thread {

    @Autowired
    @Qualifier("jskpAuditService")
    private JskpAuditService jskpAuditService;
    private String threadName;

    private int lastId = 0;

    @Override
    public void run() {
        threadName = Thread.currentThread().getName();
        System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " start.......");
        task();
        System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " end.......");
//        jskpAuditService.setLastId(lastId);
    }

    public void task() {
        int total = 0;
        int pages = 0;
        int pageSize = 100;

        if (jskpAuditService == null) {
            jskpAuditService = (JskpAuditService) SpringUtil.getBean("jskpAuditService");
        }
        if (jskpAuditService == null) {
            jskpAuditService = new JskpAuditServiceImpl();
        }
        total = jskpAuditService.count();
        pages = total / pageSize;
        if (total % pageSize != 0) {
            pages++;
        }

        try {
            for (int i = 0; i < pages; i++) {
//                if (System.currentTimeMillis() - startTime > limitTime) {
//                    System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + ":超出时间限制！");
//                    setPushFinish(true);
//                    return;
//                }
                List<Map<String, Object>> list = jskpAuditService.queryPage(i * pageSize, pageSize);
                jskpAuditService.pushRedis(list);
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            setPushFinish(true);
        }

    }
}
