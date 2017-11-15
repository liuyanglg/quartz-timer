package com.zjaxn.jobs.support;

import com.base.BaseTest;
import com.zjaxn.jobs.utils.SpringUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class JskpHttpApiTest extends BaseTest{
    @Test
    public void getCardByTaxid() throws Exception {
        JskpHttpApi.getCardByTaxid("330624770720792");
    }

    @Test
    public void getCardByName() throws Exception {
        JskpHttpApi.getCardByName("新昌县天润果业有限公司");
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