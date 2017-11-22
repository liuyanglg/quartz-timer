package com.zjaxn.jobs.support;

import com.alibaba.fastjson.JSON;
import com.zjaxn.jobs.utils.SpringUtil;
import com.zjaxn.jobs.utils.model.JskpCard;
import com.zjaxn.jobs.utils.model.JskpCardAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.sql.Connection;
import java.util.*;

import static com.zjaxn.jobs.tasks.JskpAutoAuditTask.pushFinish;

public class JskpAuditThread implements Runnable {

    public static final Integer UNAUDIT = 0;
    public static final Integer PASS = 1;
    public static final Integer UNPASS = -1;

    private String threadName = null;

    private Jedis jedis = null;

    @Autowired
    @Qualifier("jskpRedisConnectionFactory")
    private JedisConnectionFactory jskpRedisConnectionFactory;

    @Override
    public void run() {
        threadName = " [" + Thread.currentThread().getName() + "] ";

        System.out.println(threadName+" start！");
        batchAutoAudit();
        System.out.println(threadName+" end！");
    }

    public void batchAutoAudit() {
        boolean exit = true;
        while (exit) {
            List<JskpCardAudit> list = lpopRedis();
            if (list != null && list.size() > 0) {
                auditData(list);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (pushFinish) {
                    exit = false;
                }
            }
        }
    }

    public List<JskpCardAudit> lpopRedis() {
        jedis = getJedis();
        if (null == jedis) {
            return null;
        }

        Map config = (Map) SpringUtil.getBean("jskpCmpApiMap");
        String redisKey = (String) config.get("jskp.redis.auto.aduit.key");

        List<JskpCardAudit> list = null;

        int batchSize = 2;


        Set<Response<String>> responseSet = new LinkedHashSet<Response<String>>();
        Pipeline pipeline = jedis.pipelined();

        for (int i = 0; i < batchSize; i++) {
            Response<String> response = pipeline.lpop(redisKey);

            if (response == null) {
                break;
            }
            responseSet.add(response);
        }
        pipeline.sync();

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
        if (list != null) {
            System.out.println(threadName+": fetch " +list.size());
        }
        return list;
    }

    public void auditData(List<JskpCardAudit> list) {
        if (null == list) {
            return;
        }

        StringBuilder sql = null;
        try {
            Connection conn = ConnectionFactory.getConnection("dataserver");
            for (JskpCardAudit cardAudit : list) {
                String code = cardAudit.getCode();
                String taxid = cardAudit.getTaxid();
                String name = cardAudit.getName();

                if (taxid != null && name != null) {
                    sql = new StringBuilder("SELECT COUNT(1) FROM mongo_complany WHERE ");
                    sql.append(" credit_code='" + taxid + "'");
                    sql.append(" AND name='" + name + "'");

                    int find = JskpJdbcUtil.count(conn, sql.toString());

                    if (find <= 0) {
                        JskpHttpApi.updateAuditStatus(cardAudit.getId(), UNPASS);

                    } else {
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
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Jedis getJedis() {
//        JedisPool jedisPool = (JedisPool) SpringUtil.getBean("jskpJedisPool");
        jskpRedisConnectionFactory = (JedisConnectionFactory) SpringUtil.getBean("jskpRedisConnectionFactory");
//        Jedis jedis = jedisPool.getResource();
        if(jedis==null){
            jedis = jskpRedisConnectionFactory.getConnection().getNativeConnection();
        }
        return jedis;
    }
}
