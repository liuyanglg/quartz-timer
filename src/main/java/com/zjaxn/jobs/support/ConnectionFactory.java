package com.zjaxn.jobs.support;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class ConnectionFactory {
    private static ConnectionFactory connectionFactory;
    private Map<String, DataSource> dataSources;

    @PostConstruct
    private void init() {
        connectionFactory = this;
    }

    public Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public static Connection getConnection(String dataSourceName) throws SQLException {
        DataSource source = connectionFactory.dataSources.get(dataSourceName);
        if (source == null) {
            System.out.println("获取[" + dataSourceName + "]数据源失败");
            throw new SQLException("获取[" + dataSourceName + "]数据源失败");
        }
        return source.getConnection();
    }

    public static void closeDataSource(String dataSourceName) throws SQLException {
        DataSource source = connectionFactory.dataSources.get(dataSourceName);
        if (source == null) {
            System.out.println("关闭连接失败!");
            throw new SQLException("关闭连接失败");
        }
        source.getConnection().close();
    }
}
