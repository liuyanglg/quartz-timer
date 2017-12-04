package com.zjaxn.jobs.service;


import com.alibaba.fastjson.JSON;
import com.zjaxn.jobs.dao.JskpAuditDAO;
import com.zjaxn.jobs.service.util.SpringUtils;
import com.zjaxn.jobs.support.JskpApiResponse;
import com.zjaxn.jobs.support.JskpHttpApi;
import com.zjaxn.jobs.temp.PropertiesUtil;
import com.zjaxn.jobs.utils.model.JskpCard;
import com.zjaxn.jobs.utils.model.JskpCardAudit;
import org.apache.log4j.Logger;
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
    private static Logger LOG = Logger.getLogger(JskpAuditServiceImpl.class);

    public static final Integer UNAUDIT = 0;
    public static final Integer PASS = 1;
    public static final Integer UNPASS = -1;
    private int lastId = 0;

    @Autowired
    @Qualifier("jskpAuditDAO")
    private JskpAuditDAO jskpAuditDAO;

    @Autowired
    @Qualifier("jskpBigDataJedisPool")
    private JedisPool jskpBigDataJedisPool;

    @Autowired
    @Qualifier("jskpAutoAuditJedisPool")
    private JedisPool jskpAutoAuditJedisPool;

    /**
     * @method : getLastId
     * @description :从res/resource/jskp/jskp-query-offset.properties获取上次处理数据的id
     * @return : int
     * @author : liuya
     * @date : 2017-11-23 星期四 09:57:26
     */
    public int getLastId() {
        if (lastId == 0) {
            lastId = getLastOffset();
        }
        return lastId;
    }

    /**
     * @method : setLastId
     * @description : 更新文件为本次处理数据的id
     * @param lastId :
     * @return : void
     * @author : liuya
     * @date : 2017-11-23 星期四 09:59:13
     */
    public void setLastId(int lastId) {
        if (this.lastId != lastId) {
            updateLastOffset(lastId);
        }
        this.lastId = lastId;
    }

    /**
     * @method : queryPage
     * @description : 查询审核库为处理过的审核数据
     * @param offset :
     * @param pageSize :
     * @return : java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @author : liuya
     * @date : 2017-11-23 星期四 10:00:36
     */
    public List<Map<String, Object>> queryPage(int offset, int pageSize) {
        List<Map<String, Object>> list = null;
        if (lastId != 0) {
            lastId = getLastOffset();
        }

        String queryPageSql = "SELECT A.id,A.code,A.taxid,A.name,A.address,A.telephone,A.bank,A.account,A.source,A.type ,M.taxid as ctaxid,M.name as cname\n" +
                "FROM tb_cmp_card_audit A LEFT JOIN tb_cmp_card M ON A.code = M.code WHERE A.status=0 AND A.id>? LIMIT ?,?;";

        queryPageSql = queryPageSql.replaceFirst("\\?", lastId + "");
        try {
            list = jskpAuditDAO.queryPage(queryPageSql, offset, pageSize);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @method : count
     * @description : 查询审核库为处理过的审核数据的数量
     * @return : int
     * @author : liuya
     * @date : 2017-11-23 星期四 10:01:22
     */
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
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    /**
     * @method : checkICBC
     * @description : 根据MongoDB中对应工商信息数据判断审核结果
     * @param taxid : 税号
     * @param name :  企业名称
     * @return : int 审核结果
     * @author : liuya
     * @date : 2017-11-23 星期四 10:01:59
     */
    public int checkICBC(String taxid, String name) {
        if (name == null || taxid == null) {
            return UNAUDIT;
        }

        int find = UNAUDIT;

        try {
            Map<String, String> map = null;
            if (name != null) {
                map = jskpAuditDAO.queryByName(name);
            }

            if (map == null && taxid != null) {
                map = jskpAuditDAO.queryByTaxid(taxid);
            }

            if (map != null) {
                String nameDB = map.get("_id");

                String taxidDB = map.get("统一社会信用代码");

                if (taxidDB == null) {
                    taxidDB = map.get("注册号");
                }

                if ((taxidDB != null && taxidDB.equals(taxid)) && (nameDB != null && nameDB.equals(name))) {
                    find = PASS;
                } else {
                    find = UNPASS;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            LOG.error(e.getMessage());
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
            LOG.error(e.getMessage());
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

    /**
     * @method : pushRedis
     * @description : 将待审核数据放入redis缓存
     * @param list :
     * @return : void
     * @author : liuya
     * @date : 2017-11-23 星期四 10:04:00
     */
    public void pushRedis(List<Map<String, Object>> list) {
        boolean flag = true;
        Jedis jedis = null;
        try {
            jedis = jskpAutoAuditJedisPool.getResource();

            Map config = (Map) SpringUtils.getBean("jskpCmpApiMap");
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
            LOG.error(e.getMessage());
            e.printStackTrace();
        } finally {
            close(jskpAutoAuditJedisPool, jedis, flag);
        }
    }

    /**
     * @method : pushBigDataRedis
     * @description : 将待审核数据放入大数据redis缓存
     * @param list :
     * @return : void
     * @author : liuya
     * @date : 2017-11-23 星期四 10:04:50
     */
    public void pushBigDataRedis(List<Map<String, Object>> list) {
        boolean flag = true;
        Jedis jedis = null;
        try {
            jedis = jskpBigDataJedisPool.getResource();

            Map config = (Map) SpringUtils.getBean("jskpCmpApiMap");
            String redisKey = (String) config.get("jskp.redis.big.data.key");
            Pipeline pipeline = jedis.pipelined();
            for (Map map : list) {
                Map mergeMap = mergeCardAudit(map);
                if (mergeMap != null && mergeMap.size() > 0) {
                    pipeline.sadd(redisKey, JSON.toJSONString(mergeMap));
                }
            }
            pipeline.sync();
        } catch (Exception e) {
            flag = false;
            LOG.error(e.getMessage());
            e.printStackTrace();
        } finally {
            close(jskpBigDataJedisPool, jedis, flag);
        }
    }

    /**
     * @method : popRedis
     * @description : 取出指定数量的待审核数据
     * @param batchSize :
     * @return : java.util.List<com.zjaxn.jobs.utils.model.JskpCardAudit>
     * @author : liuya
     * @date : 2017-11-23 星期四 10:05:29
     */
    public List<JskpCardAudit> popRedis(int batchSize) {
        boolean flag = true;
        List<JskpCardAudit> list = null;
        Jedis jedis = null;
        try {
            jedis = jskpAutoAuditJedisPool.getResource();
            Map config = (Map) SpringUtils.getBean("jskpCmpApiMap");
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
            LOG.error(e.getMessage());
            e.printStackTrace();
        } finally {
            close(jskpAutoAuditJedisPool, jedis, flag);
        }
        return list;
    }


    /**
     * @method : auditData
     * @description : 审核数据
     * @param list :
     * @return : void
     * @author : liuya
     * @date : 2017-11-23 星期四 10:06:08
     */
    public void auditData(List<JskpCardAudit> list) {
        if (null == list) {
            return;
        }

        try {
            for (JskpCardAudit cardAudit : list) {
                String code = cardAudit.getCode();
                String taxid = cardAudit.getTaxid();
                String name = cardAudit.getName();

                JskpApiResponse<JskpCard> cardResponse=null;

                if(code==null){
                    cardResponse= JskpHttpApi.getCardByName(name);
                }
                if(cardResponse==null||!cardResponse.getCode().equals("200")){
                    cardResponse= JskpHttpApi.getCardByTaxid(taxid);
                }
                if(cardResponse!=null&&cardResponse.getCode().equals("200")){
                    JskpCard card = cardResponse.getData();
                    if(card!=null){
                        code = card.getCode();
                        cardAudit.setCode(code);
                    }
                }

                if (taxid != null && name != null) {
                    int find = checkICBC(taxid, name);

                    if (find == UNPASS) {
                        JskpHttpApi.updateAuditStatus(cardAudit.getId(), UNPASS);

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
                        }

                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @method : mergeCardAudit
     * @description : 将缺失的审核数据补全
     * @param map :
     * @return : java.util.Map
     * @author : liuya
     * @date : 2017-11-23 星期四 10:06:25
     */
    public Map mergeCardAudit(Map map) {
        if (map == null || map.size() <= 0) {
            return null;
        }

        Map<String, Object> mergeMap = new LinkedHashMap<String, Object>();
        Integer id = (Integer) map.get("id");
        String code = (String) map.get("code");
        String taxid = (String) map.get("taxid");
        String name = (String) map.get("name");
        String address = (String) map.get("address");
        String telephone = (String) map.get("telephone");
        String bank = (String) map.get("bank");
        String account = (String) map.get("account");
        Integer source = (Integer) map.get("source");
        Integer type = (Integer) map.get("type");

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
        mergeMap.put("address", address);
        mergeMap.put("telephone", telephone);
        mergeMap.put("bank", bank);
        mergeMap.put("account", account);
        mergeMap.put("source", source);
        mergeMap.put("type", type);

        return mergeMap;
    }

    public void close(JedisPool jedisPool, Jedis jedis, boolean flag) {
        if (jedis != null) {
            if (!flag) {
                jedisPool.returnResource(jedis);
            } else {
                jedisPool.returnBrokenResource(jedis);
            }
            jedis.close();
        }
    }

    public void close(JedisPool jedisPool, Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
            jedis.close();
        }
    }
}
