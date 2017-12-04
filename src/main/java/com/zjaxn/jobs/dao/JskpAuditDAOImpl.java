package com.zjaxn.jobs.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zjaxn.jobs.support.JskpMongoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository("jskpAuditDAO")
public class JskpAuditDAOImpl implements JskpAuditDAO {

    @Autowired
    @Qualifier("jskpJdbcTemplate")
    private JdbcTemplate jskpJdbcTemplate;

    public List<Map<String, Object>> queryPage(String sql, int offset, int pageSize) throws SQLException {
        return jskpJdbcTemplate.queryForList(sql, offset, pageSize);
    }

    public int count(String sql) throws SQLException {
        return jskpJdbcTemplate.queryForInt(sql);
    }

    public int checkDB(String sql) throws SQLException {
        return jskpJdbcTemplate.queryForInt(sql);
    }


    public Map<String, String> queryByName(String name) throws Exception {
        String fieldName = "_id";

        String json = JskpMongoUtil.getInstance().getData(fieldName, name);

        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, new TypeReference<Map<String, String>>() {
        });
    }

    public Map<String, String> queryByTaxid(String taxid) throws Exception {
        String fieldName = "统一社会信用代码";

        String json = JskpMongoUtil.getInstance().getData(fieldName, taxid);

        if (json == null || json.trim().length() == 0) {
            fieldName = "注册号";
            json = JskpMongoUtil.getInstance().getData(fieldName, taxid);
        }

        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, new TypeReference<Map<String, String>>() {
        });
    }
}
