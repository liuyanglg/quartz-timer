package com.zjaxn.jobs.support;

import com.zjaxn.jobs.service.JskpAuditService;
import com.zjaxn.jobs.service.JskpAuditServiceImpl;
import com.zjaxn.jobs.tasks.JskpAutoAuditTask;
import com.zjaxn.jobs.utils.DateUtil;
import com.zjaxn.jobs.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.zjaxn.jobs.tasks.JskpAutoAuditTask.limitTime;


public class JskpAuditPushThread extends Thread {

    @Autowired
    @Qualifier("jskpAuditService")
    private JskpAuditService jskpAuditService;
    private String threadName;
    private int lastId = 0;

    int total = 0;
    int pages = 0;
    int pageSize = 100;

    private long startTime;

    public JskpAuditPushThread(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public void run() {
        threadName = Thread.currentThread().getName();
        System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " push start.......");
        try {
            task();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            JskpAutoAuditTask.countDown();
            jskpAuditService.setLastId(lastId);
            System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " push end.......");
        }
    }

    public void task() throws Exception {
        if (jskpAuditService == null) {
            jskpAuditService = (JskpAuditService) SpringUtil.getBean("jskpAuditService");
        }
        if (jskpAuditService == null) {
            jskpAuditService = new JskpAuditServiceImpl();
        }
        lastId = jskpAuditService.getLastId();

        total = jskpAuditService.count();
        pages = total / pageSize;
        if (total % pageSize != 0) {
            pages++;
        }

        for (int i = 0; i < pages; i++) {
            List<Map<String, Object>> list = jskpAuditService.queryPage(i * pageSize, pageSize);
            if (list != null && list.size() > 0) {
                if (System.currentTimeMillis() - startTime > limitTime) {
                    System.out.println("[" + DateUtil.format(new Date()) + "]" + threadName + " 超出限制时长，停止审核！");
                    break;
                }

                jskpAuditService.pushRedis(list);
                jskpAuditService.pushBigDataRedis(list);
                Map<String, Object> map = list.get(list.size() - 1);
                lastId = (Integer) map.get("id");
                System.out.println("the last query id:" + lastId);
            }
        }
    }
}
