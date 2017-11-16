package com.zjaxn.jobs.tasks;

import com.zjaxn.jobs.support.ConnectionFactory;
import com.zjaxn.jobs.support.JskpApiResponse;
import com.zjaxn.jobs.support.JskpHttpApi;
import com.zjaxn.jobs.support.JskpJdbcUtil;
import com.zjaxn.jobs.utils.model.JskpCard;
import com.zjaxn.jobs.utils.model.JskpCardAudit;

import java.sql.Connection;
import java.sql.SQLException;
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

        JskpApiResponse apiResponse = null;
        try {
            if (icbcTaxid != null) {
                apiResponse = JskpHttpApi.getCardAuditByTaxid(icbcTaxid);
            }
            if (icbcName != null && (apiResponse == null || !apiResponse.getCode().equals("200"))) {
                apiResponse = JskpHttpApi.getCardAuditByTaxid(icbcName);
            }

            if (apiResponse != null && apiResponse.getCode().equals("200")) {
                cardAudit = apiResponse.getJavaObject(JskpCardAudit.class);
            }
        } catch (Exception e) {
            System.out.println("查询审核数据出错：" + e.getMessage());
            e.printStackTrace();
        }
        return cardAudit;
    }

    public JskpCard getCard(JskpCardAudit cardAudit) {
        if (cardAudit == null) {
            return null;
        }

        JskpCard card = null;
        JskpApiResponse apiResponse = null;
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
                card = apiResponse.getJavaObject(JskpCard.class);
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

    public void audit(Map<String, Object> icbcInfoMap) {
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
            if (cardAudit.getTaxid().equals(icbcTaxid) && cardAudit.getName().equals(icbcName)) {
                JskpHttpApi.updateAuditStatus(cardAudit.getId(), PASS);
                if (cardAudit.getCode() == null || cardAudit.getCode().trim().length() == 0) {
                    JskpHttpApi.addCard(cardAudit.toJson());
                } else if (card != null && (!card.getTaxid().equals(cardAudit.getTaxid()) || !card.getName().equals(cardAudit.getName()))) {
                    JskpHttpApi.updateCard(cardAudit.getCode(), cardAudit.toJson());
                }
            } else {
                JskpHttpApi.updateAuditStatus(cardAudit.getId(), UNPASS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
