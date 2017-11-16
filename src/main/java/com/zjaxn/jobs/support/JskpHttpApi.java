package com.zjaxn.jobs.support;

import com.alibaba.fastjson.JSON;
import com.zjaxn.jobs.utils.SpringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Map;

public class JskpHttpApi {

    public static JskpApiResponse getCardAuditByTaxid(String taxid) throws Exception {
        if (taxid == null) {
            return null;
        }
        if (taxid.trim().length() <= 0) {
            return null;
        }

        Map apiConfig = (Map) SpringUtil.getBean("jskpCmpApiMap");
        JskpApiResponse apiResponse = null;
        String url = (String) apiConfig.get("jskp.cmp.url") + apiConfig.get("jskp.cmp.api.get.getCardAuditByTaxid");
        url = url.replaceFirst("\\{\\S*\\}", taxid.trim());

        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url.trim());
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                apiResponse = JSON.parseObject(result, JskpApiResponse.class);
            }
        } finally {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        }
        return apiResponse;
    }

    public static JskpApiResponse getCardAuditByName(String name) throws Exception {
        if (name == null) {
            return null;
        }
        if (name.trim().length() <= 0) {
            return null;
        }

        Map apiConfig = (Map) SpringUtil.getBean("jskpCmpApiMap");
        JskpApiResponse apiResponse = null;
        String url = (String) apiConfig.get("jskp.cmp.url") + apiConfig.get("jskp.cmp.api.get.getCardAuditByName");
        url = url.replaceFirst("\\{\\S*\\}", name.trim());

        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url.trim());
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                apiResponse = JSON.parseObject(result, JskpApiResponse.class);
            }
        } finally {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        }
        return apiResponse;
    }

    public static JskpApiResponse getCardByCode(String code) throws Exception {
        if (code == null) {
            return null;
        }
        if (code.trim().length() != 6) {
            return null;
        }

        Map apiConfig = (Map) SpringUtil.getBean("jskpCmpApiMap");
        JskpApiResponse apiResponse = null;
        String url = (String) apiConfig.get("jskp.cmp.url") + apiConfig.get("jskp.cmp.api.get.getCardByCode");
        url = url.replaceFirst("\\{\\S*\\}", code.trim());

        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url.trim());
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                apiResponse = JSON.parseObject(result, JskpApiResponse.class);
            }
        } finally {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        }
        return apiResponse;
    }

    public static JskpApiResponse getCardByTaxid(String taxid) throws Exception {
        if (taxid == null) {
            return null;
        }
        if (taxid.trim().length() <= 0) {
            return null;
        }

        Map apiConfig = (Map) SpringUtil.getBean("jskpCmpApiMap");
        JskpApiResponse apiResponse = null;
        String url = (String) apiConfig.get("jskp.cmp.url") + apiConfig.get("jskp.cmp.api.get.getCardByTaxid");
        url = url.replaceFirst("\\{\\S*\\}", taxid.trim());

        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url.trim());
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                apiResponse = JSON.parseObject(result, JskpApiResponse.class);
            }
        } finally {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        }
        return apiResponse;
    }

    public static JskpApiResponse getCardByName(String name) throws Exception {
        if (name == null) {
            return null;
        }
        if (name.trim().length() <= 0) {
            return null;
        }

        Map apiConfig = (Map) SpringUtil.getBean("jskpCmpApiMap");
        JskpApiResponse apiResponse = null;
        String url = (String) apiConfig.get("jskp.cmp.url") + apiConfig.get("jskp.cmp.api.get.getCardByName");
        url = url.replaceFirst("\\{\\S*\\}", name.trim());

        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url.trim());
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                apiResponse = JSON.parseObject(result, JskpApiResponse.class);
            }
        } finally {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        }
        return apiResponse;
    }


    public static JskpApiResponse updateAuditStatus(Integer id, Integer status) throws Exception {
        if (id == null) {
            return null;
        }
        if (status == null) {
            return null;
        }

        Map apiConfig = (Map) SpringUtil.getBean("jskpCmpApiMap");
        JskpApiResponse apiResponse = null;
        String url = (String) apiConfig.get("jskp.cmp.url") + apiConfig.get("jskp.cmp.api.post.updateAuditStatus");
        url = url.replaceFirst("\\{\\S*\\}", id + "");

        String requestJson = "{\"status\":%d}";

        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url.trim());
            StringEntity requestEntity = new StringEntity(String.format(requestJson, status), "utf-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                apiResponse = JSON.parseObject(result, JskpApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        }
        return apiResponse;
    }

    public static JskpApiResponse addCard(String json) throws Exception {
        if (json == null) {
            return null;
        }

        Map apiConfig = (Map) SpringUtil.getBean("jskpCmpApiMap");
        JskpApiResponse apiResponse = null;
        String url = (String) apiConfig.get("jskp.cmp.url") + apiConfig.get("jskp.cmp.api.post.addCard");

        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url.trim());
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                apiResponse = JSON.parseObject(result, JskpApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        }
        return apiResponse;
    }

    public static JskpApiResponse updateCard(String code,String json) throws Exception {
        if(code==null){
            return null;
        }
        if (json == null) {
            return null;
        }

        Map apiConfig = (Map) SpringUtil.getBean("jskpCmpApiMap");
        JskpApiResponse apiResponse = null;
        String url = (String) apiConfig.get("jskp.cmp.url") + apiConfig.get("jskp.cmp.api.put.updateCard");
        url = url.replaceFirst("\\{\\S*\\}", code.trim());

        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            HttpPut httpPut = new HttpPut(url.trim());
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            httpPut.setHeader("Content-type", "application/json");
            httpPut.setEntity(requestEntity);
            HttpResponse httpResponse = closeableHttpClient.execute(httpPut);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                apiResponse = JSON.parseObject(result, JskpApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        }
        return apiResponse;
    }
}
