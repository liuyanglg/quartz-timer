package com.zjaxn.jobs.support;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


/**
 * Created by hwz on 2017-11-20.
 */
public class JskpMongoDBVisitor {
    private static MongoDatabase mongoDB;
    private static MongoClient mongoClient;

    /**
     * mongodb初始化
     * @param serverAddr    服务器地址和端口号(如：192.168.1.1:27017）
     * @param databaseName 数据库名
     * @param userName   用户名
     * @param password   密码
     */
    public static void openDB(String serverAddr, String databaseName, String userName, String password) {
        List<ServerAddress> serverAddrList = new ArrayList<ServerAddress>();
        ServerAddress serverAddress = new ServerAddress(serverAddr);
        serverAddrList.add(serverAddress);
        List<MongoCredential> credentialList = new ArrayList<MongoCredential>();
        MongoCredential credential = MongoCredential.createCredential(userName, databaseName, password.toCharArray());
        credentialList.add(credential);

        mongoClient = new MongoClient(serverAddrList, credentialList);
        mongoDB = mongoClient.getDatabase(databaseName);
    }

    /**
     * mongodb初始化
     * @param tableName    表名
     * @param searchFieldName 查询字段名
     * @param searchFieldValue   查询字段值
     * @return json数据
     *
     */
    public static String getData(String tableName, String searchFieldName, String searchFieldValue) {
        try {
            MongoCollection<Document> collection = mongoDB.getCollection(tableName, Document.class);
            Document document = collection.find().first();

            document = collection.find(eq(searchFieldName, searchFieldValue)).first();
//            System.out.println("query:  "+document.toJson());
            return document.toJson();
        } catch (Exception ex) {
            //写入日志
        }

        return null;
    }

    public static void closeDB() {
        mongoClient.close();
    }

}
