package com.zjaxn.jobs.tasks;

import com.zjaxn.jobs.service.JskpAuditService;
import com.zjaxn.jobs.support.JskpAuditPopThread;
import com.zjaxn.jobs.support.JskpAuditPushThread;
import com.zjaxn.jobs.support.JskpMongoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JskpAutoAuditTask implements Runnable {
        public static long limitTime = 1000 * 60 * 60 * 5L;
//    public static long limitTime = 1000 * 6;
    public static volatile int threadCounter = 3;
    public static volatile int threadNum = 2;


    @Autowired
    @Qualifier("jskpAuditService")
    private JskpAuditService jskpAuditService;

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        JskpAuditPushThread pushThread = null;

        pushThread = new JskpAuditPushThread(startTime);
        pushThread.start();
        while (threadCounter > 2) {
            try {
                System.out.println("等待push完成：" + threadCounter);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ExecutorService pool = Executors.newFixedThreadPool(threadCounter);
        for (int t = 0; t < threadNum; t++) {
            pool.execute(new JskpAuditPopThread(startTime));
        }

        while (threadCounter > 0) {
            try {
                Thread.sleep(5000);
                System.out.println("等待pop结束:  " + threadCounter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
        }
        JskpMongoUtil.getInstance().close();

        long endTime = System.currentTimeMillis();
        System.out.println("用时：" + getTimeString(endTime - startTime));
    }

    public static synchronized void countDown() {
        System.out.println("counter:" + threadCounter);
        threadCounter--;
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
