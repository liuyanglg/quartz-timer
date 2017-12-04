package com.zjaxn.jobs.tasks;

import com.zjaxn.jobs.support.JskpAuditPopThread;
import com.zjaxn.jobs.support.JskpAuditPushThread;
import com.zjaxn.jobs.utils.DateUtil;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @package : com.zjaxn.jobs.tasks
 * @class : JskpAutoAuditTask
 * @description : 自动审核任务
 * @author : liuya
 * @date : 2017-11-23 星期四 10:18:32
 * @version : V1.0.0
 * @copyright : 2017 liuya Inc. All rights reserved.
 */
public class JskpAutoAuditTask extends QuartzJobBean {
    private static Logger LOG = Logger.getLogger(JskpAutoAuditTask.class);

    public static long timeOut = 1000 * 60 * 60 * 5L;
    private static volatile int counter = 3;//线程计数
    private static volatile int auditThreadNum = 2;//审核线程数
    private long startTime;
    private long endTime;

    public static void setTimeOut(long timeOut) {
        JskpAutoAuditTask.timeOut = timeOut;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("[" + DateUtil.format(new Date()) + "] jskp:" + " 极速开票自动审核任务开始......");
        LOG.error("[" + DateUtil.format(new Date()) + "] jskp:" + " 极速开票自动审核任务开始......");
        startTime = System.currentTimeMillis();

        task();

        endTime = System.currentTimeMillis();
        System.out.println("[" + DateUtil.format(new Date()) + "] jskp:" + " 极速开票自动审核任务结束，用时：" + getTimeString(endTime - startTime));
        LOG.error("[" + DateUtil.format(new Date()) + "] jskp:" + " 极速开票自动审核任务结束，用时：" + getTimeString(endTime - startTime));
    }

    public void task() {
        counter = 3;
        auditThreadNum = 2;

        JskpAuditPushThread pushThread = new JskpAuditPushThread(startTime);
        pushThread.start();

        while (counter > 2) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                LOG.error(e.getMessage());
                e.printStackTrace();
            }
        }

        ExecutorService pool = Executors.newFixedThreadPool(counter);
        for (int t = 0; t < auditThreadNum; t++) {
            pool.execute(new JskpAuditPopThread(startTime));
        }

        while (counter > 0) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                LOG.error(e.getMessage());
                e.printStackTrace();
            }
        }
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
        }
    }

    public String getTimeString(Long useTime) {
        StringBuffer timeBuffer = new StringBuffer();
        long ms = useTime % 1000;
        long sec = (useTime / (1000)) % (60);
        long min = (useTime / (1000 * 60)) % (60);
        long hour = useTime / (1000 * 60 * 60);
        if (hour > 0) {
            timeBuffer.append(hour + "时");
        }
        if (min > 0) {
            timeBuffer.append(min + "分");
        }
        if (sec > 0) {
            timeBuffer.append(sec + "秒");
        }
        if (ms > 0) {
            timeBuffer.append(ms + "毫秒");
        }
        return timeBuffer.toString();
    }

    public static synchronized void countDown() {
        counter--;
    }
}
