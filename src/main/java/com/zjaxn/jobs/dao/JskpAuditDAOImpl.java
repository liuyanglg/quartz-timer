package com.zjaxn.jobs.dao;

import com.zjaxn.jobs.support.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository("jskpAuditDAO")
public class JskpAuditDAOImpl implements JskpAuditDAO {

    @Autowired
    @Qualifier("jskpJdbcTemplate")
    private JdbcTemplate jskpJdbcTemplate;
    public List<Map<String, Object>> queryPage(String sql, int offset, int pageSize) throws SQLException {
        Connection conn = ConnectionFactory.getConnection("dataserver");
        return jskpJdbcTemplate.queryForList(sql,offset,pageSize);
    }

    public int count(String sql) throws SQLException {
        Connection conn = ConnectionFactory.getConnection("dataserver");
        return jskpJdbcTemplate.queryForInt(sql);
    }

    public int checkDB(String sql) throws SQLException {
        Connection conn = ConnectionFactory.getConnection("dataserver");
        return jskpJdbcTemplate.queryForInt(sql);
    }
}
