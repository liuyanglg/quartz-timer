package com.zjaxn.jobs.tasks;

import com.zjaxn.jobs.support.ConnectionFactory;
import com.zjaxn.jobs.support.JskpApiResponse;
import com.zjaxn.jobs.support.JskpHttpApi;
import com.zjaxn.jobs.support.JskpJdbcUtil;
import com.zjaxn.jobs.temp.PropertiesUtil;
import com.zjaxn.jobs.utils.DateUtil;
import com.zjaxn.jobs.utils.model.JskpCard;
import com.zjaxn.jobs.utils.model.JskpCardAudit;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class JskpAutoAuditTask {
    public static Logger log = Logger.getLogger(JskpAutoAuditTask.class);

    public static final Integer UNAUDIT = 0;
    public static final Integer PASS = 1;
    public static final Integer UNPASS = -1;

    private Properties properties;
    private String key = "jskp.cmp.last.offset";
    private String path = File.separator + "res" + File.separator + "resource" + File.separator + "jskp" + File.separator + "jskp-query-offset.properties";
    private Long limitTime = 1000 * 60 * 60 * 5L;

    public void batchAutoAudit() {
        System.out.println(DateUtil.format(new Date()) + " 自动审核任务开始，限时为：" + getTimeString(limitTime) + "  ......");
        log.warn(DateUtil.format(new Date()) + " 自动审核任务开始，限时为：" + getTimeString(limitTime) + "......");
        Long startTime = System.currentTimeMillis();

        String queryPageSql = "SELECT *  FROM mongo_complany LIMIT ?,?;";
        String countSql = "SELECT COUNT(1)  FROM mongo_complany ;";
        List<Map<String, Object>> list = null;
        int lastOffset = getLastOffset();
        int auditCounter = 0;
        int total = 0;
        int pageSize = 100;
        int pages = 0;

        try {
            Connection conn = ConnectionFactory.getConnection("dataserver");
            total = JskpJdbcUtil.count(conn, countSql);
            total -= lastOffset;
            System.out.println(DateUtil.format(new Date()) + " 待处理工商信息数据：" + total + "条");
            log.warn(DateUtil.format(new Date()) + " 待处理工商信息数据：" + total + "条");

            pages = total / pageSize;
            if (total % pageSize != 0) {
                pages++;
            }
            for (int i = 0; i < pages; i++) {
                list = JskpJdbcUtil.queryPage(conn, queryPageSql, lastOffset + i * pageSize, pageSize);

                if (i % 20 == 0) {
                    System.out.println("已处理数据：" + auditCounter);
                    log.warn("已处理数据：" + auditCounter);
                }

                if (list != null) {
                    for (Map map : list) {
                        singleAudit(map);
                        auditCounter++;
                    }
                }

                Long interruptTime = System.currentTimeMillis();

                if (interruptTime - startTime >= limitTime) {
                    System.out.println(DateUtil.format(new Date()) + " 审核用时：" + getTimeString(interruptTime - startTime) + "，已超过时长限制，停止审核");
                    log.warn(DateUtil.format(new Date()) + " 审核用时：" + getTimeString(interruptTime - startTime) + "，已超过时长限制，停止审核");
                    break;
                }

                Thread.sleep(1);
            }

        } catch (SQLException e) {
            System.out.println(DateUtil.format(new Date()) + "查询工商信息出错：" + e.getMessage());
            log.warn(DateUtil.format(new Date()) + "查询工商信息出错：" + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println(DateUtil.format(new Date()) + e.getMessage());
            e.printStackTrace();
        } finally {
            updateLastOffset(lastOffset + auditCounter);
        }
        Long endTime = System.currentTimeMillis();
        System.out.println(DateUtil.format(new Date()) + " 此次处理了" + auditCounter + "条工商信息数据，耗时为：" + getTimeString(endTime - startTime));
        log.warn(DateUtil.format(new Date()) + " 此次处理了" + auditCounter + "条工商信息数据，耗时为：" + getTimeString(endTime - startTime));
    }

    public JskpCardAudit getCardAudit(Map<String, Object> icbcInfoMap) {
        JskpCardAudit cardAudit = null;
        if (icbcInfoMap == null) {
            return null;
        }
        String icbcName = (String) icbcInfoMap.get("name");
        String icbcTaxid = (String) icbcInfoMap.get("credit_code");

        JskpApiResponse<JskpCardAudit> apiResponse = null;
        try {
            if (icbcTaxid != null) {
                apiResponse = JskpHttpApi.getCardAuditByTaxid(icbcTaxid);
            }
            if (icbcName != null && (apiResponse == null || !apiResponse.getCode().equals("200"))) {
                apiResponse = JskpHttpApi.getCardAuditByName(icbcName);
            }

            if (apiResponse != null && apiResponse.getCode().equals("200")) {
                cardAudit = apiResponse.getData();
            }
        } catch (Exception e) {
            System.out.println("查询审核数据出错：" + e.getMessage());
            log.warn("查询审核数据出错：" + e.getMessage());
            e.printStackTrace();
        }
        return cardAudit;
    }

    public JskpCard getCard(JskpCardAudit cardAudit) {
        if (cardAudit == null) {
            return null;
        }

        JskpCard card = null;
        JskpApiResponse<JskpCard> apiResponse = null;
        try {
            if (cardAudit.getCode() != null && cardAudit.getCode().trim().length() == 6) {
                apiResponse = JskpHttpApi.getCardByCode(cardAudit.getCode());
            } else {
                if (cardAudit.getTaxid() != null) {
                    apiResponse = JskpHttpApi.getCardByTaxid(cardAudit.getTaxid());
                } else if (cardAudit.getName() != null && (apiResponse == null || !apiResponse.getCode().equals("200"))) {
                    apiResponse = JskpHttpApi.getCardByName(cardAudit.getName());
                }
            }

            if (apiResponse != null && apiResponse.getCode().equals("200")) {
                card = apiResponse.getData();
            }
        } catch (Exception e) {
            System.out.println("查询正式库数据出错：" + e.getMessage());
            log.warn("查询正式库数据出错：" + e.getMessage());
            e.printStackTrace();
        }

        return card;
    }

    public JskpCardAudit mergeCardAudit(JskpCardAudit cardAudit, JskpCard card) {
        if ((cardAudit.getTaxid() == null || cardAudit.getTaxid().trim().length() == 0) && (cardAudit.getName() == null || cardAudit.getName().trim().length() == 0)) {
            return cardAudit;
        }

        if (card != null) {
            if (cardAudit.getTaxid() == null) {
                cardAudit.setTaxid(card.getTaxid());
            }
            if (cardAudit.getName() == null) {
                cardAudit.setName(card.getName());
            }
        }

        return cardAudit;
    }

    public void singleAudit(Map<String, Object> icbcInfoMap) {
        if (icbcInfoMap == null) {
            return;
        }
        String icbcName = (String) icbcInfoMap.get("name");
        String icbcTaxid = (String) icbcInfoMap.get("credit_code");
        JskpCardAudit cardAudit = getCardAudit(icbcInfoMap);
        JskpCard card = getCard(cardAudit);
        if (cardAudit == null) {
            return;
        }

        cardAudit = mergeCardAudit(cardAudit, card);

        try {
            if (equals(icbcTaxid, cardAudit.getTaxid()) && equals(icbcName, cardAudit.getName())) {
                boolean success = true;

                if (card == null && (cardAudit.getCode() == null || cardAudit.getCode().trim().length() == 0)) {
                    JskpApiResponse<JskpCard> apiResponse = JskpHttpApi.addCard(cardAudit.toJson());
                    if (apiResponse == null || !apiResponse.getCode().equals("201")) {
                        success = false;
                    }
                } else if (card != null && (!equals(cardAudit.getTaxid(), card.getTaxid()) || !equals(cardAudit.getName(), card.getName()))) {
                    JskpApiResponse<JskpCard> apiResponse = JskpHttpApi.updateCard(cardAudit.getCode(), cardAudit.toJson());
                    if (apiResponse == null || !apiResponse.getCode().equals("201")) {
                        success = false;
                    }
                }

                if (success) {
                    JskpHttpApi.updateAuditStatus(cardAudit.getId(), PASS);
                }

            } else {
                JskpHttpApi.updateAuditStatus(cardAudit.getId(), UNPASS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getLastOffset() {
        Integer offset = null;
        try {
            properties = PropertiesUtil.loadProperty(PropertiesUtil.getProjectPath() + path);
            String offsetStr = properties.getProperty(key);
            offset = Integer.parseInt(offsetStr);
        } catch (NumberFormatException e) {
            offset = null;
            e.printStackTrace();
        }
        return offset;
    }

    public void updateLastOffset(Integer offset) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, String.valueOf(offset));
        PropertiesUtil.updateProperty(properties, PropertiesUtil.getProjectPath() + path, map);
    }

    boolean equals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 != null && s2 == null) {
            return false;
        }
        if (s1 == null && s2 != null) {
            return false;
        }
        if (s1.equals(s2)) {
            return true;
        }
        return false;
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
