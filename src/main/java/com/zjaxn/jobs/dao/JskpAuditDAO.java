package com.zjaxn.jobs.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface JskpAuditDAO {
    List<Map<String,Object>> queryPage(String sql, int offset, int pageSize)throws SQLException;

    int count(String sql)throws SQLException;

    int checkDB(String sql) throws SQLException;

    int checkMongoDB(String taxid, String name) throws Exception;
}
