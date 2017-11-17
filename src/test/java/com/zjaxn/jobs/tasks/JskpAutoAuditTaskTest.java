package com.zjaxn.jobs.tasks;

import com.base.BaseTest;
import com.zjaxn.jobs.utils.DateUtil;
import org.junit.Test;

import java.util.Date;

public class JskpAutoAuditTaskTest extends BaseTest {
    @Test
    public void autoAuditCard() throws Exception {
        System.out.println(DateUtil.format(new Date()) + " 自动审核任务开始......");
        /*更新用户中心每天新增的数据*/
        Long timeStart1 = System.currentTimeMillis();
        JskpAutoAuditTask jskpAutoAuditTask = new JskpAutoAuditTask();
        jskpAutoAuditTask.autoAuditCard();
        Long timeEnd1 = System.currentTimeMillis();
        System.out.println(DateUtil.format(new Date()) + " 更新完毕，耗时为：" + getTimeString(timeEnd1 - timeStart1));
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
        timeBuffer.append(ms + "毫秒");
        return timeBuffer.toString();
    }
}