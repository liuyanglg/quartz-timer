package com.zjaxn.jobs.support;

import com.zjaxn.jobs.service.JskpAuditService;
import com.zjaxn.jobs.service.JskpAuditServiceImpl;
import com.zjaxn.jobs.service.util.SpringUtils;
import com.zjaxn.jobs.tasks.JskpAutoAuditTask;
import com.zjaxn.jobs.utils.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.zjaxn.jobs.tasks.JskpAutoAuditTask.timeOut;

/**
 * @package : com.zjaxn.jobs.support
 * @class : JskpAuditPushThread
 * @description : 将未审核数据放入redis缓存线程
 * @author : liuya
 * @date : 2017-11-23 星期四 10:08:02
 * @version : V1.0.0
 * @copyright : 2017 liuya Inc. All rights reserved.
 */
public class JskpAuditPushThread extends Thread {
    private static Logger LOG = Logger.getLogger(JskpAuditPushThread.class);

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
        System.out.println("[" + DateUtil.format(new Date()) + "] jskp:" + threadName + " 把未审核数据放入redis，开始.......");
        LOG.error("[" + DateUtil.format(new Date()) + "] jskp:" + threadName + " 把未审核数据放入redis，开始.......");
        try {
            task();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            JskpAutoAuditTask.countDown();
            jskpAuditService.setLastId(lastId);
            System.out.println("[" + DateUtil.format(new Date()) + "] jskp:" + threadName + " 把未审核数据放入redis，完成！");
            LOG.error("[" + DateUtil.format(new Date()) + "] jskp:" + threadName + " 把未审核数据放入redis，完成！");
        }
    }

    public void task() throws Exception {
        if (jskpAuditService == null) {
            jskpAuditService = (JskpAuditService) SpringUtils.getBean("jskpAuditService");
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
                if (System.currentTimeMillis() - startTime > timeOut) {
                    System.out.println("[" + DateUtil.format(new Date()) + "] jskp:" + threadName + " 超出限制时长，停止审核！");
                    LOG.error("[" + DateUtil.format(new Date()) + "] jskp:" + threadName + " 超出限制时长，停止审核！");
                    break;
                }

                jskpAuditService.pushRedis(list);
                jskpAuditService.pushBigDataRedis(list);
                Map<String, Object> map = list.get(list.size() - 1);
                lastId = (Integer) map.get("id");
                Thread.sleep(1);
            }
        }
    }
}
