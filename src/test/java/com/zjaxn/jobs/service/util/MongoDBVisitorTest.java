package com.zjaxn.jobs.service.util;

import com.base.BaseTest;
import com.zjaxn.jobs.support.JskpMongoDBConfig;
import com.zjaxn.jobs.support.JskpMongoDBVisitor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MongoDBVisitorTest extends BaseTest {
    @Autowired
    @Qualifier("jskpMongoDBConfig")
    private JskpMongoDBConfig jskpMongoDBConfig;

    @Test
    public void openDB() throws Exception {
        JskpMongoDBVisitor.openDB(jskpMongoDBConfig.getServerAdress(), jskpMongoDBConfig.getDatabase(), jskpMongoDBConfig.getUserName(), jskpMongoDBConfig.getPassword());
    }

    @Test
    public void getData() throws Exception {
        JskpMongoDBVisitor.openDB(jskpMongoDBConfig.getServerAdress(), jskpMongoDBConfig.getDatabase(), jskpMongoDBConfig.getUserName(), jskpMongoDBConfig.getPassword());
        String taxid = "_id";
        String taxidValue = "泉州市洛江万虹闽海石化有限公司";
//        String taxid = "credit_code";
//        String taxidValue = "913505040732108260";
        JskpMongoDBVisitor.getData(jskpMongoDBConfig.getTablename(), taxid, taxidValue);
        JskpMongoDBVisitor.closeDB();
    }

    @Test
    public void closeDB() throws Exception {
    }

}