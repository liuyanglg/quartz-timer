package com.zjaxn.jobs.support;

import com.alibaba.fastjson.JSON;
import com.zjaxn.jobs.tasks.JskpAutoAuditTask;
import com.zjaxn.jobs.temp.PropertiesUtil;
import com.zjaxn.jobs.utils.SpringUtil;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static com.zjaxn.jobs.tasks.JskpAutoAuditTask.limitTime;

public class JskpPushThread extends Thread {

    private String threadName = null;
    public static Logger log = Logger.getLogger(JskpPushThread.class);
    public static Jedis jedis = null;

    @Override
    public void run() {
        threadName = " [" + Thread.currentThread().getName() + "] ";

        System.out.println(threadName + " start！");
        pushQueryData();
    }

    public void pushQueryData() {
        long timeStart = System.currentTimeMillis();
        String queryPageSql = "SELECT A.id,A.code,A.taxid,A.name ,M.taxid as ctaxid,M.name as cname\n" +
                "FROM tb_cmp_card_audit A LEFT JOIN tb_cmp_card M ON A.code = M.code WHERE A.status=0 AND A.id>? LIMIT ?,?;";
        String countSql = "SELECT COUNT(1)\n" +
                "FROM tb_cmp_card_audit A LEFT JOIN tb_cmp_card M ON A.code = M.code WHERE A.status=0 AND A.id>?;";

        List<Map<String, Object>> list = null;
        int total = 0;
        int pages = 0;
        int pageSize = 4;
        Object lastId = 0;

        try {
            Connection conn = ConnectionFactory.getConnection("dataserver");
            lastId = getLastOffset();
            queryPageSql = queryPageSql.replaceFirst("\\?", lastId + "");
            countSql = countSql.replaceFirst("\\?", lastId + "");

            total = JskpJdbcUtil.count(conn, countSql);
            pages = total / pageSize;
            if (total % pageSize != 0) {
                pages++;
            }

            for (int i = 0; i < pages; i++) {
                list = JskpJdbcUtil.queryPage(conn, queryPageSql, i * pageSize, pageSize);
                pushRedis(list);
                System.out.println(threadName + ": push " + pageSize);
                log.warn(threadName + ": push " + pageSize);
                long timeEnd = System.currentTimeMillis();
                lastId = list.get(list.size() - 1).get("id");

                if (timeEnd - timeStart > limitTime) {
                    System.out.println("超出执行时长限制！");
                    break;
                }
            }
            updateLastOffset(lastId);

            JskpAutoAuditTask.setPushFinish(true);

        } catch (SQLException e) {
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

    public void pushRedis(List<Map<String, Object>> list) {
        Jedis jedis = getJedis();
        if (null == jedis) {
            return;
        }

        Map config = (Map) SpringUtil.getBean("jskpCmpApiMap");
        String redisKey = (String) config.get("jskp.redis.auto.aduit.key");
        Pipeline pipeline = jedis.pipelined();
        for (Map map : list) {
            Map mergeMap = mergeCardAudit(map);
            if (mergeMap != null && mergeMap.size() > 0) {
//                String json = JSON.toJSONString(mergeMap);
//                System.out.println(json);
                pipeline.rpush(redisKey, JSON.toJSONString(mergeMap));
            }
        }
        pipeline.sync();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public Jedis getJedis() {
        JedisPool jedisPool = (JedisPool) SpringUtil.getBean("jskpJedisPool");
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
