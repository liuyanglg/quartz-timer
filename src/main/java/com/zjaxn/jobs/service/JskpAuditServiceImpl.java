package com.zjaxn.jobs.service;


import com.alibaba.fastjson.JSON;
import com.zjaxn.jobs.dao.JskpAuditDAO;
import com.zjaxn.jobs.support.JskpApiResponse;
import com.zjaxn.jobs.support.JskpHttpApi;
import com.zjaxn.jobs.temp.PropertiesUtil;
import com.zjaxn.jobs.utils.SpringUtil;
import com.zjaxn.jobs.utils.model.JskpCard;
import com.zjaxn.jobs.utils.model.JskpCardAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.File;
import java.sql.SQLException;
import java.util.*;


@Repository("jskpAuditService")
public class JskpAuditServiceImpl implements JskpAuditService {
    public static final Integer UNAUDIT = 0;
    public static final Integer PASS = 1;
    public static final Integer UNPASS = -1;
    private int lastId = 0;

    @Autowired
    @Qualifier("jskpAuditDAO")
    private JskpAuditDAO jskpAuditDAO;

    @Autowired
    @Qualifier("jskpJedisPool")
    private JedisPool jskpJedisPool;


    public int getLastId() {
        if (lastId == 0) {
            lastId = getLastOffset();
        }
        return lastId;
    }

    public void setLastId(int lastId) {
        if (this.lastId != lastId) {
            updateLastOffset(lastId);
        }
        this.lastId = lastId;
    }

    public List<Map<String, Object>> queryPage(int offset, int pageSize) {
        List<Map<String, Object>> list = null;
        if (lastId != 0) {
            lastId = getLastOffset();
        }

        String queryPageSql = "SELECT A.id,A.code,A.taxid,A.name ,M.taxid as ctaxid,M.name as cname\n" +
                "FROM tb_cmp_card_audit A LEFT JOIN tb_cmp_card M ON A.code = M.code WHERE A.status=0 AND A.id>? LIMIT ?,?;";

        queryPageSql = queryPageSql.replaceFirst("\\?", lastId + "");
//        System.out.println("sql: " + queryPageSql);
        try {
            list = jskpAuditDAO.queryPage(queryPageSql, offset, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int count() {
        int total = 0;
        if (lastId != 0) {
            lastId = getLastOffset();
        }

        String countSql = "SELECT COUNT(1)\n" +
                "FROM tb_cmp_card_audit A LEFT JOIN tb_cmp_card M ON A.code = M.code WHERE A.status=0 AND A.id>?;";
        countSql = countSql.replaceFirst("\\?", lastId + "");

        try {
            total = jskpAuditDAO.count(countSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public int checkICBC(String taxid, String name) {
//        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM mongo_complany WHERE ");
//        sql.append(" credit_code='" + taxid + "'");
//        sql.append(" AND name='" + name + "'");
        int find = UNAUDIT;
        try {
//            find = jskpAuditDAO.checkDB(sql.toString());
            find = jskpAuditDAO.checkMongoDB(taxid, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return find;
    }


    public Integer getLastOffset() {
        String key = "jskp.cmp.last.id";
        String path = File.separator + "res" + File.separator + "resource" + File.separator + "jskp" + File.separator + "jskp-query-offset.properties";

        Properties properties = null;
        int offset = 0;
        try {
            properties = PropertiesUtil.loadProperty(PropertiesUtil.getProjectPath() + path);
            String offsetStr = properties.getProperty(key).trim();
            offset = Integer.parseInt(offsetStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return offset;
    }


    public void updateLastOffset(Object offset) {
        String key = "jskp.cmp.last.id";
        String path = File.separator + "res" + File.separator + "resource" + File.separator + "jskp" + File.separator + "jskp-query-offset.properties";
        Properties properties = PropertiesUtil.loadProperty(PropertiesUtil.getProjectPath() + path);
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, String.valueOf(offset));
        PropertiesUtil.updateProperty(properties, PropertiesUtil.getProjectPath() + path, map);
    }

    public void pushRedis(List<Map<String, Object>> list) {
        boolean flag = true;
        Jedis jedis = null;
        try {
            jedis = jskpJedisPool.getResource();

            Map config = (Map) SpringUtil.getBean("jskpCmpApiMap");
            String redisKey = (String) config.get("jskp.redis.auto.aduit.key");
            Pipeline pipeline = jedis.pipelined();
            for (Map map : list) {
                Map mergeMap = mergeCardAudit(map);
                if (mergeMap != null && mergeMap.size() > 0) {
                    pipeline.rpush(redisKey, JSON.toJSONString(mergeMap));
                }
            }
            pipeline.sync();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        } finally {
            close(jedis, flag);
        }
    }

    public List<JskpCardAudit> popRedis(int batchSize) {
        boolean flag = true;
        List<JskpCardAudit> list = null;
        Jedis jedis = null;
        try {
            jedis = jskpJedisPool.getResource();
            Map config = (Map) SpringUtil.getBean("jskpCmpApiMap");
            String redisKey = (String) config.get("jskp.redis.auto.aduit.key");

            Set<Response<String>> responseSet = new LinkedHashSet<Response<String>>();
            Pipeline pipeline = jedis.pipelined();

            for (int i = 0; i < batchSize; i++) {
                Response<String> response = pipeline.lpop(redisKey);

                responseSet.add(response);
            }
            pipeline.sync();

            for (Response<String> res : responseSet) {
                String json = res.get();
                if (json != null) {
                    JskpCardAudit cardAudit = JSON.parseObject(json, JskpCardAudit.class);

                    if (null == list) {
                        list = new ArrayList<JskpCardAudit>();
                    }
                    list.add(cardAudit);
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        } finally {
            close(jedis, flag);
        }
        return list;
    }


    public void auditData(List<JskpCardAudit> list) {
        if (null == list) {
            return;
        }

        StringBuilder sql = null;
        try {
            for (JskpCardAudit cardAudit : list) {
                String code = cardAudit.getCode();
                String taxid = cardAudit.getTaxid();
                String name = cardAudit.getName();

                if (taxid != null && name != null) {
//                    sql = new StringBuilder("SELECT COUNT(1) FROM mongo_complany WHERE ");
//                    sql.append(" credit_code='" + taxid + "'");
//                    sql.append(" AND name='" + name + "'");

                    int find = jskpAuditDAO.checkMongoDB(taxid, name);

                    if (find == UNPASS) {
                        JskpHttpApi.updateAuditStatus(cardAudit.getId(), UNPASS);
//                        System.out.println("UNPASS: " + cardAudit.getId());

                    } else if (find == PASS) {
                        boolean success = true;

                        if (code == null || code.length() != 6) {
                            JskpApiResponse<JskpCard> apiResponse = JskpHttpApi.addCard(cardAudit.toJson());
                            if (apiResponse == null || !apiResponse.getCode().equals("201")) {
                                success = false;
                            }
                        } else {
                            JskpApiResponse<JskpCard> apiResponse = JskpHttpApi.updateCard(cardAudit.getCode(), cardAudit.toJson());
                            if (apiResponse == null || !apiResponse.getCode().equals("201")) {
                                success = false;
                            }
                        }

                        if (success) {
                            JskpHttpApi.updateAuditStatus(cardAudit.getId(), PASS);
                            System.out.println("PASS: " + cardAudit.getId());
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Map mergeCardAudit(Map map) {
        if (map == null || map.size() <= 0) {
            return null;
        }

        Map<String, Object> mergeMap = new LinkedHashMap<String, Object>();
        Integer id = (Integer) map.get("id");
        String code = (String) map.get("code");
        String taxid = (String) map.get("taxid");
        String name = (String) map.get("name");

        if (taxid == null || taxid.trim().length() <= 0) {
            taxid = (String) map.get("ctaxid");
        }
        if (name == null || name.trim().length() <= 0) {
            name = (String) map.get("cname");
        }

        mergeMap.put("id", id);
        mergeMap.put("code", code);
        mergeMap.put("taxid", taxid);
        mergeMap.put("name", name);

        return mergeMap;
    }

    public void close(Jedis jedis, boolean flag) {
        if (jedis != null) {
            if (!flag) {
                jskpJedisPool.returnResource(jedis);
            } else {
                jskpJedisPool.returnBrokenResource(jedis);
            }
            jedis.close();
        }
    }

    public void close(Jedis jedis) {
        if (jedis != null) {
            jskpJedisPool.returnResource(jedis);
            jedis.close();
        }
    }
}
