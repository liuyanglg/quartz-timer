package com.zjaxn.jobs.support;


import com.zjaxn.jobs.tasks.CmpUpdateTableTask;
import com.zjaxn.jobs.utils.DateUtil;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.zjaxn.jobs.support.CmpSqlQuery.*;

/**
 * @Package : com.zjaxn.jobs.support
 * @Class : CmpUpUserCenterNewThread
 * @Description : 处理用户中心新增数据的线程类
 * @Author : liuyang
 * @CreateDate : 2017-09-15 星期五 15:28:10
 * @Version : V1.0.0
 * @Copyright : 2017 liuyang Inc. All rights reserved.
 */
public class CmpUpUserCenterNewThread extends Thread {
    private static Logger log = Logger.getLogger(CmpUpUserCenterNewThread.class);

    private int offset;
    private int pageSize;
    private int taskSize;

    private Connection cenConnection = null;
    private Connection cmpConnection = null;
    private String threadName = null;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTaskSize() {
        return taskSize;
    }

    public void setTaskSize(int taskSize) {
        this.taskSize = taskSize;
    }

    @Override
    public void run() {
        threadName = " [" + Thread.currentThread().getName() + "] ";
        try {
            updateTable();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(DateUtil.format(new Date()) + threadName + e);
            log.error(DateUtil.format(new Date()) + threadName + e);
        } finally {
            CmpJdbcUtils.close(cenConnection);
            CmpJdbcUtils.close(cmpConnection);
            CmpUpdateTableTask.countDown(threadName);//线程计数器减1
        }
    }

    /**
     * @Method : updateTable
     * @Description : 更新关系表
     * @ReturnType : void
     * @Author : liuyang
     * @CreateDate : 2017-09-15 星期五 16:29:42
     */
    private void updateTable() throws Exception {
        System.out.println(DateUtil.format(new Date()) + threadName + "start......");
        log.info(DateUtil.format(new Date()) + threadName + "start......");

        try {
            cenConnection = CmpDBManager.getConnection(CmpDataSourceEnum.CENTER);
            cmpConnection = CmpDBManager.getConnection(CmpDataSourceEnum.CMP);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(DateUtil.format(new Date()) + threadName + e);
            log.error(DateUtil.format(new Date()) + threadName + e);
        }

        int pages = 0;//分页的页数
        pages = taskSize / pageSize;
        if (taskSize % pageSize != 0) {
            pages += 1;
        }

        for (int i = 0; i < pages; i++) {
            int innerOffset = offset + i * pageSize;
            List<Map<String, Object>> ucenterNewList = null;
            StringBuffer queryCompanyUserCenterAddSql = new StringBuffer();
            queryCompanyUserCenterAddSql.append(SELECT)
                    .append(TB_U_TAX_NUM).append(AS).append(TAX_ID).append(SPLIT)
                    .append(TB_U_SERVICE_ID).append(AS).append(SERVICE_ID).append(FROM).append(TB_U)
                    .append(WHERE)
                    .append(TB_U_SERVICE_ID).append(IS_NOT_NULL).append(AND)
                    .append(TB_U_TAX_NUM).append(IS_NOT_NULL).append(AND)
                    .append(TB_U_SERVICE_ID).append("!=").append(NULL_STRING).append(AND)
                    .append(TB_U_TAX_NUM).append("!=").append(NULL_STRING).append(AND)
                    .append(TB_U_ADD_DATE).append(" >= ").append(DATE_SUB.replace(PLACEHOLDER, '1'))
                    .append(LIMIT).append(innerOffset).append(SPLIT).append(pageSize)
                    .append(END);
            try {
                ucenterNewList = CmpJdbcUtils.queryForList(cenConnection, queryCompanyUserCenterAddSql.toString());//查询用户中心新增的数据
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(DateUtil.format(new Date()) + threadName + e);
                log.error(DateUtil.format(new Date()) + threadName + e);
            }

            String[] keys = {SERVICE_ID.trim(), TAX_ID.trim(), TAX_ID.trim()};
            StringBuffer insertRelationTableSql = new StringBuffer();
            insertRelationTableSql.append(INSERT_INTO).append(TB_R)
                    .append("(").append(CODE).append(SPLIT).append(TAX_ID).append(SPLIT).append(SERVICE_ID).append(")").append("(")
                    .append(SELECT).append(DISTINCT)
                    .append(TB_A_CODE).append(AS).append(CODE).append(SPLIT)
                    .append(TB_A_TAX_ID).append(AS).append(TAX_ID).append(SPLIT)
                    .append(PLACEHOLDER).append(AS).append(SERVICE_ID)
                    .append(FROM).append(TB_A).append(LEFT).append(JOIN).append(TB_M)
                    .append(ON).append(TB_A_CODE).append(" = ").append(TB_M_CODE)
                    .append(WHERE)
                    .append(TB_M_TAX_ID).append(" = ").append(PLACEHOLDER)
                    .append(OR).append("(").append(TB_A_TAX_ID).append(" = ").append(PLACEHOLDER).append(AND).append(TB_A_CODE).append(IS_NULL).append(")")
                    .append(")")
                    .append(END);
            CmpJdbcUtils.insertBatch(cmpConnection, insertRelationTableSql.toString(), ucenterNewList, keys);
        }
    }
}
