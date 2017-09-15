package com.zjaxn.jobs.support;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;

public class CmpDBManager {
    private static CmpDBManager cmpDBManager;

    private  CmpConnectionFactory cmpConnectionFactory;

    public void setCmpConnectionFactory(CmpConnectionFactory cmpConnectionFactory) {
        this.cmpConnectionFactory = cmpConnectionFactory;
    }

    @PostConstruct
    private void  init(){
        cmpDBManager=this;
    }

    public  static Connection getConnection(CmpDataSourceEnum connectionName) throws SQLException {
        Connection connection=null;
        switch (connectionName){
            case CENTER:
                connection = cmpDBManager.cmpConnectionFactory.getConnectionCenter();
                break;
            case CMP:
                connection = cmpDBManager.cmpConnectionFactory.getConnectionCmp();
                break;
        }
        return connection;
    }

    public  static void closeDataSource() throws SQLException {
        cmpDBManager.cmpConnectionFactory.getConnectionCenter().close();
        cmpDBManager.cmpConnectionFactory.getConnectionCenter().close();
    }

}
