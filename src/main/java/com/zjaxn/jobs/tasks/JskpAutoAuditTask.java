package com.zjaxn.jobs.tasks;

import com.zjaxn.jobs.service.JskpAuditService;
import com.zjaxn.jobs.service.JskpAuditServiceImpl;
import com.zjaxn.jobs.support.JskpAuditPopThread;
import com.zjaxn.jobs.support.JskpAuditPushThread;
import com.zjaxn.jobs.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JskpAutoAuditTask implements Runnable {
    public static volatile boolean pushFinish = false;
    public static long limitTime = 1000 * 60 * 60 * 5;
    //    public static long limitTime = 1000 * 12;
    public static volatile long startTime = 0L;
    public static volatile int threadCounter = 2;
    public static volatile int pushCounter = 0;
    public static volatile int popCounter = 0;

    int total = 0;
    int pages = 0;
    int pageSize = 100;

    @Autowired
    @Qualifier("jskpAuditService")
    private JskpAuditService jskpAuditService;

    @Override
    public void run() {

        startTime = System.currentTimeMillis();

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

        for(int i=0;i<pages;i++) {

        }
        JskpAuditPushThread pushThread = new JskpAuditPushThread();
        pushThread.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ExecutorService pool = Executors.newFixedThreadPool(threadCounter);
        for (int i = 0; i < threadCounter; i++) {
            pool.execute(new JskpAuditPopThread());
        }

        boolean run = true;
        while (run) {
            try {
                if (threadCounter <= 0 && pushFinish) {
                    System.out.println("push: " + popCounter);
                    System.out.println("pop: " + popCounter);
                    run = false;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (pool != null && !pool.isShutdown()) {
                    pool.shutdown();
                }
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("用时：" + getTimeString(endTime - startTime));
    }

    public static synchronized void setPushFinish(boolean pushFinish) {
        JskpAutoAuditTask.pushFinish = pushFinish;
    }

    public static synchronized long getStartTime() {
        return startTime;
    }

    public static synchronized void countDown() {
        threadCounter--;
//        log.info(DateUtil.format(new Date()) + threadName + " finished,running thread: " + threadCounter);
    }

    public static synchronized int getPushCounter() {
        return pushCounter;
    }

    public static synchronized void setPushCounter(int pushCounter) {
        JskpAutoAuditTask.pushCounter = pushCounter;
    }

    public static synchronized int getPopCounter() {
        return popCounter;
    }

    public static synchronized void setPopCounter(int popCounter) {
        JskpAutoAuditTask.popCounter = popCounter;
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
}
