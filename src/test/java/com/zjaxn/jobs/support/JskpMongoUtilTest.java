package com.zjaxn.jobs.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.base.BaseTest;
import org.junit.Test;

import java.util.Map;

public class JskpMongoUtilTest extends BaseTest {
    @Test
    public void getInstance() throws Exception {
        for (int i = 0; i < 5; i++) {
            JskpMongoUtil mongoUtil = JskpMongoUtil.getInstance();
            String json = mongoUtil.getData("_id", "溧阳市溧城空维图文工作室");
            System.out.println(mongoUtil + ":" + 1);
        }

        JskpMongoUtil.getInstance().close();
        System.out.println("---------------------------------------------------------------");
        for (int i = 0; i < 5; i++) {
            JskpMongoUtil mongoUtil = new JskpMongoUtil();
            String json = mongoUtil.getData("_id", "无锡市林威工程机械有限公司");
            System.out.println(mongoUtil + ":" + json);
        }
    }

    @Test
    public void getInstance2() throws Exception {
        JskpMongoUtil mongoUtil = new JskpMongoUtil();
        String taxid = "913202045754275581";
        String json = mongoUtil.getData("_id", "无锡市美昌嘉制冷设备有限公司");
        Map<String, String> map = JSON.parseObject(json, new TypeReference<Map<String, String>>() {
        });
        if (map != null) {
            String taxidDB = map.get("统一社会信用代码");
            if (taxidDB == null) {
                taxidDB = map.get("注册号");
            }
            if (taxidDB != null && taxidDB.equals(taxid)) {
                if (taxidDB.equals(taxid)) {
                    System.out.println("equal: "+1);
                } else {
                    System.out.println("unequal: "+-1);
                }
            }
        }
        System.out.println(mongoUtil + ":" + json);
    }
}