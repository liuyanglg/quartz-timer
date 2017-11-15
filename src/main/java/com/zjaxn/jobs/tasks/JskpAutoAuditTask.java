package com.zjaxn.jobs.tasks;

import com.zjaxn.jobs.support.ConnectionFactory;
import com.zjaxn.jobs.support.JskpApiResponse;
import com.zjaxn.jobs.support.JskpHttpApi;
import com.zjaxn.jobs.support.JskpJdbcUtil;
import com.zjaxn.jobs.utils.model.JskpCard;
import com.zjaxn.jobs.utils.model.JskpCardAudit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JskpAutoAuditTask {
    public static final Integer UNAUDIT = 0;
    public static final Integer PASS = 1;
    public static final Integer UNPASS = -1;

    public void autoAuditCard() {
        String queryPageSql = "SELECT *  FROM mongo_complany LIMIT ?,?;";
        String countSql = "SELECT COUNT(1)  FROM mongo_complany ;";
        List<Map<String, Object>> list = null;
        int total = 0;
        int pageSize = 100;
        int pages = 0;
        try {
            Connection conn = ConnectionFactory.getConnection("dataserver");
            total = JskpJdbcUtil.count(conn, countSql);
            pages = total / pageSize;
            for (int i = 0; i < pages; i++) {
                list = JskpJdbcUtil.queryPage(conn, queryPageSql, i * pageSize, pageSize);
                if (list != null) {
                    for (Map map : list) {
                        audit(map);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("查询工商信息出错：" + e.getMessage());
            e.printStackTrace();
        }
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
            } else if (icbcName != null) {
                apiResponse = JskpHttpApi.getCardAuditByTaxid(icbcName);
            }

            if (apiResponse != null && apiResponse.getCode().equals("200")) {
                cardAudit = apiResponse.getData();
            }
        } catch (Exception e) {
            System.out.println("查询审核数据出错：" + e.getMessage());
            e.printStackTrace();
        }
        return cardAudit;
    }

    public JskpCardAudit mergeCardAudit(JskpCardAudit cardAudit) {
        if ((cardAudit.getTaxid() == null || cardAudit.getTaxid().trim().length() == 0) && (cardAudit.getName() == null || cardAudit.getName().trim().length() == 0)) {
            return cardAudit;
        }

        JskpCard card = null;
        JskpApiResponse<JskpCard> apiResponse = null;
        try {
            if (cardAudit.getName() == null) {
                apiResponse = JskpHttpApi.getCardByTaxid(cardAudit.getTaxid());
            } else if (cardAudit.getTaxid() == null) {
                apiResponse = JskpHttpApi.getCardByName(cardAudit.getName());
            }

            if (apiResponse != null && apiResponse.getCode().equals("200")) {
                card = apiResponse.getData();
            }

            if (card != null) {
                if (cardAudit.getTaxid() == null) {
                    cardAudit.setTaxid(card.getTaxid());
                }
                if (cardAudit.getName() == null) {
                    cardAudit.setName(card.getName());
                }
            }
        } catch (Exception e) {
            System.out.println("查询正式库数据出错：" + e.getMessage());
            e.printStackTrace();
        }

        return cardAudit;
    }

    public void audit(Map<String, Object> icbcInfoMap) {
        if (icbcInfoMap == null) {
            return;
        }
        String icbcName = (String) icbcInfoMap.get("name");
        String icbcTaxid = (String) icbcInfoMap.get("credit_code");
        JskpCardAudit cardAudit = getCardAudit(icbcInfoMap);
        if (cardAudit == null) {
            return;
        }
        cardAudit = mergeCardAudit(cardAudit);

        try {
            if (cardAudit.getTaxid().equals(icbcTaxid) && cardAudit.getName().equals(icbcName)) {
                JskpHttpApi.updateAuditStatus(cardAudit.getId(), PASS);
                if (cardAudit.getCode() == null || cardAudit.getCode().trim().length() == 0) {
                    JskpHttpApi.addCard(cardAudit.toJson());
                }
            } else {
                JskpHttpApi.updateAuditStatus(cardAudit.getId(), UNPASS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
