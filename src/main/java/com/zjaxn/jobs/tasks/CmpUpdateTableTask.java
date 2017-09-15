package com.zjaxn.jobs.tasks;

import com.zjaxn.jobs.support.*;
import com.zjaxn.jobs.utils.DateUtil;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static com.zjaxn.jobs.support.CmpSqlQuery.*;


/**
 * @Package : com.zjaxn.jobs.tasks
 * @Class : CmpUpdateTableTask
 * @Description : 定时任务类
 * @Author : liuyang
 * @CreateDate : 2017-09-15 星期五 16:29:07
 * @Version : V1.0.0
 * @Copyright : 2017 liuyang Inc. All rights reserved.
 */
public class CmpUpdateTableTask extends QuartzJobBean {
    public static Logger log = Logger.getLogger(CmpUpdateTableTask.class);

    public static volatile int threadCounter = 0;
    private static Connection centerConnection = null;
    private static Connection cmpConnection = null;
    private int pageSize = 500;


    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @Method : executeInternal
     * @Description : quartz 定时任务启动函数
     * @param jobExecutionContext :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:04:59
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if(pageSize==0){
            pageSize=500;
        }
        System.out.println(DateUtil.format(new Date()) + " 更新任务开始......");
        log.info(DateUtil.format(new Date()) + " 更新任务开始......");
        /*更新用户中心每天新增的数据*/
        Long timeStart1 = System.currentTimeMillis();

        int batchThreadNum = 5;
        threadCounter += 2 * batchThreadNum;

        updateCompanyAuditAdd(pageSize, batchThreadNum);
        updateUserCenterAdd(pageSize, batchThreadNum);

        destroy();//释放连接池
        Long timeEnd1 = System.currentTimeMillis();
        System.out.println(DateUtil.format(new Date()) + " 更新完毕，耗时为：" + getTimeString(timeEnd1 - timeStart1));
        log.info(DateUtil.format(new Date()) + " 更新完毕，耗时为：" + getTimeString(timeEnd1 - timeStart1));
    }

    /**
     * @Method : updateCompanyAuditAdd
     * @Description : 更新审核库新增数据
     * @Param pageSize :
     * @Param threadNumber :
     * @ReturnType : void
     * @Author : liuyang
     * @CreateDate : 2017-09-15 星期五 16:28:34
     */
    public void updateCompanyAuditAdd(int pageSize, int threadNumber) {
        int totalSize = 0;
        StringBuffer countAuditAddSql = new StringBuffer();
        countAuditAddSql.append(SELECT).append(COUNT)
                .append(FROM).append(TB_A).append(LEFT).append(JOIN).append(TB_M)
                .append(ON).append(TB_A_CODE).append(" = ").append(TB_M_CODE)
                .append(WHERE)
                .append(TB_A_CODE).append("!=").append(NULL_STRING).append(AND)
                .append(TB_A_CREATE_TIME).append(" >= ").append(DATE_SUB.replace(PLACEHOLDER, '1'))
                .append(END);
        try {
            cmpConnection = CmpDBManager.getConnection(CmpDataSourceEnum.CMP);
            totalSize = CmpJdbcUtils.count(countAuditAddSql.toString(), cmpConnection);
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(DateUtil.format(new Date())+e.getMessage());
        } finally {
            CmpJdbcUtils.close(cmpConnection);
        }

        int taskSize = 0;//每个线程处理的任务量
        int remain = 0;
        if (totalSize % (pageSize * threadNumber) == 0) {
            taskSize = totalSize / threadNumber;
        } else {
            remain = totalSize % (pageSize * threadNumber);
            taskSize = (totalSize - remain) / threadNumber;
        }

        for (int i = 0; i < threadNumber; i++) {
            CmpUpCompanyAuditNewThread thread = new CmpUpCompanyAuditNewThread();
            if (i < threadNumber - 1) {
                thread.setTaskSize(taskSize);
            } else {
                thread.setTaskSize(taskSize + remain);
            }
            thread.setOffset(i * taskSize);
            thread.setPageSize(pageSize);
            thread.start();
        }
    }


    /**
     * @Method : updateUserCenterAdd
     * @Description : 更新用户中心新增数据
     * @Param pageSize :
     * @Param threadNumber :
     * @ReturnType : void
     * @Author : liuyang
     * @CreateDate : 2017-09-15 星期五 16:28:00
     */
    public void updateUserCenterAdd(int pageSize, int threadNumber) {
        int totalSize = 0;
        StringBuffer countUserCenterAddSql = new StringBuffer();
        countUserCenterAddSql.append(SELECT).append(COUNT)
                .append(FROM).append(TB_U)
                .append(WHERE)
                .append(TB_U_TAX_NUM).append(IS_NOT_NULL).append(AND)
                .append(TB_U_SERVICE_ID).append(IS_NOT_NULL).append(AND)
                .append(TB_U_TAX_NUM).append("!=").append(NULL_STRING).append(AND)
                .append(TB_U_SERVICE_ID).append("!=").append(NULL_STRING).append(AND)
                .append(TB_U_ADD_DATE).append(" >= ").append(DATE_SUB.replace(PLACEHOLDER, '1'))
                .append(END);
        try {
            centerConnection = CmpDBManager.getConnection(CmpDataSourceEnum.CENTER);
            totalSize = CmpJdbcUtils.count(countUserCenterAddSql.toString(), centerConnection);
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(DateUtil.format(new Date())+e.getMessage());
        } finally {
            CmpJdbcUtils.close(centerConnection);
        }

        int taskSize = 0;//每个线程处理的任务量
        int remain = 0;
        if (totalSize % (pageSize * threadNumber) == 0) {
            taskSize = totalSize / threadNumber;
        } else {
            remain = totalSize % (pageSize * threadNumber);
            taskSize = (totalSize - remain) / threadNumber;
        }

        for (int i = 0; i < threadNumber; i++) {
            CmpUpUserCenterNewThread thread = new CmpUpUserCenterNewThread();
            if (i < threadNumber - 1) {
                thread.setTaskSize(taskSize);
            } else {
                thread.setTaskSize(taskSize + remain);
            }
            thread.setOffset(i * taskSize);
            thread.setPageSize(pageSize);
            thread.start();
        }
    }

    /**
     * @Method : getTimeString
     * @Description : 将毫秒转换为小时
     * @Param useTime :
     * @ReturnType : java.lang.String
     * @Author : liuyang
     * @CreateDate : 2017-08-19 星期六 19:52:18
     */
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

    /**
     * @Method : countDown
     * @Description : 线程同步计数器
     * @param threadName :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:06:30
     */
    public static synchronized void countDown(String threadName) {
        threadCounter--;
        System.out.println(DateUtil.format(new Date()) + threadName + " finished,running thread: " + threadCounter);
        log.info(DateUtil.format(new Date()) + threadName + " finished,running thread: " + threadCounter);
    }

    /**
     * @Method : destroy
     * @Description : 阻塞当前线程，并销毁连接
     * @ReturnType : void
     * @Author : liuyang
     * @CreateDate : 2017-09-15 星期五 16:27:10
     */
    public void destroy() {
        while (threadCounter > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error(DateUtil.format(new Date())+e.getMessage());
            }
        }
        try {
            CmpDBManager.closeDataSource();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(DateUtil.format(new Date())+e.getMessage());
        }
    }

}
