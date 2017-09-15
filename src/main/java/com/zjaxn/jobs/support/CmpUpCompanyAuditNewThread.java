package com.zjaxn.jobs.support;


import com.zjaxn.jobs.tasks.CmpUpdateTableTask;
import com.zjaxn.jobs.utils.DateUtil;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.zjaxn.jobs.support.CmpSqlWords.*;

/**
 * @Package : com.zjaxn.jobs.support
 * @Class : CmpUpCompanyAuditNewThread
 * @Description : 处理用户中心新增数据的线程类
 * @Author : liuyang
 * @CreateDate : 2017-09-15 星期五 15:28:34
 * @Version : V1.0.0
 * @Copyright : 2017 liuyang Inc. All rights reserved.
 */
public class CmpUpCompanyAuditNewThread extends Thread {
    private static Logger log = Logger.getLogger(CmpUpCompanyAuditNewThread.class);

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
     * @CreateDate : 2017-09-15 星期五 16:33:38
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
            List<Map<String, Object>> auditNewList = null;
            StringBuffer queryCompanyAuditAddSql = new StringBuffer();
            queryCompanyAuditAddSql.append(SELECT)
                    .append(TB_A_CODE).append(AS).append(CODE).append(SPLIT).
                    append(TB_A_TAX_ID).append(AS).append(TAX_ID).append(SPLIT)
                    .append(TB_M_TAX_ID).append(AS).append(TAX_ID_ALIAS)
                    .append(FROM).append(TB_A).append(LEFT).append(JOIN).append(TB_M)
                    .append(ON).append(TB_A_CODE).append(" = ").append(TB_M_CODE)
                    .append(WHERE)
                    .append(TB_A_CODE).append("!=").append(NULL_STRING).append(AND)
                    .append(TB_A_CREATE_TIME).append(" >= ").append(DATE_SUB.replace(PLACEHOLDER, '1'))
                    .append(LIMIT).append(innerOffset).append(SPLIT).append(pageSize)
                    .append(END);

            auditNewList = CmpJdbcUtils.queryForList(cmpConnection, queryCompanyAuditAddSql.toString());

            List<Map<String, Object>> relationList = new ArrayList<Map<String, Object>>();
            StringBuffer queryUserCenterOldSql = null;
            for (Map<String, Object> map : auditNewList) {
                queryUserCenterOldSql = new StringBuffer();
                String taxId = null;
                if (map.get(CODE.trim()) == null) {
                    taxId = TAX_ID.trim();
                } else {
                    taxId = TAX_ID_ALIAS.trim();
                }
                queryUserCenterOldSql.append(SELECT)
                        .append(toSqlString(map.get(CODE.trim()))).append(AS).append(CODE).append(SPLIT)
                        .append(toSqlString(map.get(TAX_ID.trim()))).append(AS).append(TAX_ID).append(SPLIT)
                        .append(TB_U_SERVICE_ID).append(AS).append(SERVICE_ID)
                        .append(FROM).append(TB_U)
                        .append(WHERE)
                        .append(TB_U_TAX_NUM).append(" = ").append(toSqlString(map.get(taxId))).append(AND)
                        .append(TB_U_SERVICE_ID).append(IS_NOT_NULL).append(AND)
                        .append(TB_U_TAX_NUM).append("!=").append(NULL_STRING).append(AND)
                        .append(TB_U_SERVICE_ID).append("!=").append(NULL_STRING).append(AND)
                        .append(TB_U_ADD_DATE).append(" < ").append(DATE_SUB.replace('?', '1'))
                        .append(END);
                List<Map<String, Object>> singleList = null;//

                singleList = CmpJdbcUtils.queryForList(cenConnection, queryUserCenterOldSql.toString());
                if (singleList.size() > 0) {
                    relationList.addAll(singleList);
                }
            }

            StringBuffer insertRelationTableSql = new StringBuffer();
            insertRelationTableSql.append(INSERT_INTO).append(TB_R)
                    .append("(").append(CODE).append(SPLIT).append(TAX_ID).append(SPLIT).append(SERVICE_ID).append(")")
                    .append(VALUE).append("(").append(PLACEHOLDER).append(SPLIT).append(PLACEHOLDER).append(SPLIT).append(PLACEHOLDER).append(")")
                    .append(END);
            String[] keys = {CODE.trim(), TAX_ID.trim(), SERVICE_ID.trim()};

            CmpJdbcUtils.insertBatch(cmpConnection, insertRelationTableSql.toString(), relationList, keys);
        }

    }

    /**
     * @Method : toSqlString
     * @Description : sql语句字段值加引号
     * @Param str :
     * @ReturnType : java.lang.String
     * @Author : liuyang
     * @CreateDate : 2017-09-15 星期五 16:35:59
     */
    private String toSqlString(Object str) {
        if (str != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("'").append((String) str).append("'");
            return buffer.toString();
        }
        return null;
    }
}

