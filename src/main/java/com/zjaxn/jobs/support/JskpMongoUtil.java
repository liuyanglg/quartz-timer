package com.zjaxn.jobs.support;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zjaxn.jobs.utils.SpringUtil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class JskpMongoUtil {
    private  MongoDatabase mongoDB;
    private  MongoClient mongoClient;

    private JskpMongoDBConfig jskpMongoDBConfig;

    public static final class JsckpMongoUtilHolder {
        public static final JskpMongoUtil INSTANCE = new JskpMongoUtil();
    }

    public static JskpMongoUtil getInstance() {
        return JsckpMongoUtilHolder.INSTANCE;
    }

    private void init() {
        if (jskpMongoDBConfig == null) {
            jskpMongoDBConfig = (JskpMongoDBConfig) SpringUtil.getBean("jskpMongoDBConfig");
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

    public  String getData(String fieldName, String fieldValue) {
        if (mongoDB == null) {
            init();
        }

        try {
            MongoCollection<Document> collection = mongoDB.getCollection(jskpMongoDBConfig.getTablename(), Document.class);
            Document document = collection.find().first();
            document = collection.find(eq(fieldName, fieldValue)).first();
//            System.out.println("query:  "+document.toJson());
            return document.toJson();
        } catch (Exception ex) {
            //写入日志
        }
        return null;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

}
