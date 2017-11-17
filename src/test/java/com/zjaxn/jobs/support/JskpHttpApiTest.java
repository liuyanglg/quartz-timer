package com.zjaxn.jobs.support;

import com.base.BaseTest;
import com.zjaxn.jobs.utils.model.JskpCard;
import org.junit.Test;

public class JskpHttpApiTest extends BaseTest {

    @Test
    public void addCard() throws Exception {
    }

    @Test
    public void updateCard() throws Exception {
        String json = "{\"code\":\"F6FBD3\",\"taxid\":\"330624770720792\",\"name\":\"新昌县天润果业有限公司\",\"address\":\"小将镇迭里村\",\"telephone\":\"13858590190\",\"bank\":\"\",\"account\":\"\",\"type\":\"0\",\"cert\":\"0\",\"source\":\"20\",\"status\":\"0\"}";
        JskpHttpApi.updateCard("F6FBD3", json);
    }

    @Test
    public void getCardByTaxid() throws Exception {
        JskpApiResponse jskpApiResponse = JskpHttpApi.getCardByTaxid("330624770720792");
    }

    @Test
    public void getCardByName() throws Exception {
        JskpHttpApi.getCardByName("玉环县德福水性涂料商行");
    }

    @Test
    public void getCardByCode() throws Exception {
        JskpApiResponse<JskpCard> apiResponse = JskpHttpApi.getCardByCode("C3US7U");
//        JskpCard card = apiResponse.getJavaObject(JskpCard.class);
        JskpCard card = apiResponse.getData();
        System.out.println(card);
    }

    @Test
    public void updateAuditStatus() throws Exception {
        JskpHttpApi.updateAuditStatus(297, 1);
    }

    @Test
    public void getCardAuditByName() throws Exception {
        JskpHttpApi.getCardAuditByName("航信培训企业");
    }

    @Test
    public void getCardAuditByTaxid() throws Exception {
        JskpHttpApi api = new JskpHttpApi();
        api.getCardAuditByTaxid("112233445566779");
    }

}