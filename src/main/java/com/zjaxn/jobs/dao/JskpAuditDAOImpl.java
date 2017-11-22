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

import static com.zjaxn.jobs.service.JskpAuditServiceImpl.PASS;
import static com.zjaxn.jobs.service.JskpAuditServiceImpl.UNAUDIT;
import static com.zjaxn.jobs.service.JskpAuditServiceImpl.UNPASS;

@Repository("jskpAuditDAO")
public class JskpAuditDAOImpl implements JskpAuditDAO {

    @Autowired
    @Qualifier("jskpJdbcTemplate")
    private JdbcTemplate jskpJdbcTemplate;

//    @Autowired
//    @Qualifier("jskpMongoDBConfig")
//    private JskpMongoDBConfig jskpMongoDBConfig;

    public List<Map<String, Object>> queryPage(String sql, int offset, int pageSize) throws SQLException {
//        Connection conn = ConnectionFactory.getConnection("dataserver");
        return jskpJdbcTemplate.queryForList(sql, offset, pageSize);
    }

    public int count(String sql) throws SQLException {
//        Connection conn = ConnectionFactory.getConnection("dataserver");
        return jskpJdbcTemplate.queryForInt(sql);
    }

    public int checkDB(String sql) throws SQLException {
//        Connection conn = ConnectionFactory.getConnection("dataserver");
        return jskpJdbcTemplate.queryForInt(sql);
    }

    public int checkMongoDB(String taxid, String name) throws Exception {
        if (name == null || taxid == null) {
            return UNAUDIT;
        }

//        JskpMongoDBVisitor.openDB(jskpMongoDBConfig.getServerAdress(), jskpMongoDBConfig.getDatabase(), jskpMongoDBConfig.getUserName(), jskpMongoDBConfig.getPassword());
        String fieldName = "_id";
//        String json = JskpMongoDBVisitor.getData(jskpMongoDBConfig.getTablename(), fieldName, name);
        String json = JskpMongoUtil.getInstance().getData(fieldName, name);
//        JskpMongoDBVisitor.closeDB();
        if (json == null || json.trim().length() <= 0) {
            return UNAUDIT;
        }

        Map<String, String> map = JSON.parseObject(json, new TypeReference<Map<String, String>>() {
        });
        if (map != null) {
            String taxidDB = map.get("统一社会信用代码");
            if (taxidDB == null) {
                taxidDB = map.get("注册号");
            }
            if (taxidDB != null && taxidDB.equals(taxid)) {
                if (taxidDB.equals(taxid)) {
                    return PASS;
                } else {
                    return UNPASS;
                }
            }
        }

        return UNAUDIT;
    }
}
