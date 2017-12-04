package com.zjaxn.jobs.support;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zjaxn.jobs.service.util.SpringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * @package : com.zjaxn.jobs.support
 * @class : JskpMongoUtil
 * @description : mongoDB简单的工具类，采用单例模式
 * @author : liuya
 * @date : 2017-11-23 星期四 10:15:53
 * @version : V1.0.0
 * @copyright : 2017 liuya Inc. All rights reserved.
 */
public class JskpMongoUtil {
    private static Logger LOG = Logger.getLogger(JskpMongoUtil.class);

    private MongoDatabase mongoDB;
    private MongoClient mongoClient;

    private JskpMongoDBConfig jskpMongoDBConfig;

    public static final class JsckpMongoUtilHolder {
        public static final JskpMongoUtil INSTANCE = new JskpMongoUtil();
    }

    public static JskpMongoUtil getInstance() {
        return JsckpMongoUtilHolder.INSTANCE;
    }

    private void init() {
        if (jskpMongoDBConfig == null) {
            jskpMongoDBConfig = (JskpMongoDBConfig) SpringUtils.getBean("jskpMongoDBConfig");
        }
        List<ServerAddress> serverAddrList = new ArrayList<ServerAddress>();
        ServerAddress serverAddress = new ServerAddress(jskpMongoDBConfig.getServerAdress());
        serverAddrList.add(serverAddress);
        List<MongoCredential> credentialList = new ArrayList<MongoCredential>();
        MongoCredential credential = MongoCredential.createCredential(jskpMongoDBConfig.getUserName(), jskpMongoDBConfig.getDatabase(), jskpMongoDBConfig.getPassword().toCharArray());
        credentialList.add(credential);

        mongoClient = new MongoClient(serverAddrList, credentialList);
        mongoDB = mongoClient.getDatabase(jskpMongoDBConfig.getDatabase());
    }

    public String getData(String fieldName, String fieldValue) {
        if (mongoDB == null) {
            init();
        }

        try {
            MongoCollection<Document> collection = mongoDB.getCollection(jskpMongoDBConfig.getTablename(), Document.class);
            Document document = collection.find().first();
            document = collection.find(eq(fieldName, fieldValue)).first();
            if (document != null) {
                return document.toJson();
            }
            return null;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            LOG.error(ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

}
