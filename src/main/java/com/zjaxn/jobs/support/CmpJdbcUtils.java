package com.zjaxn.jobs.support;

import com.alibaba.druid.util.JdbcUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * @Package : com.aisino.mysql.uitls
 * @Class : CmpJdbcUtils
 * @Description :
 * @Author : liuyang
 * @CreateDate : 2017-08-18 星期五 22:00:54
 * @Version : V1.0.0
 * @Copyright : 2017 liuyang Inc. All rights reserved.
 */
public class CmpJdbcUtils {
    private static Logger log = Logger.getLogger(CmpJdbcUtils.class);

    /**
     * @Method : getConnection
     * @Description : 获取jdbc数据库连接
     * @param driver : 
     * @param url : 
     * @param username : 
     * @param password : 
     * @Return : java.sql.Connection
     * @Author : liuyang
     * @CreateDate : 2017-08-18 星期五 22:01:11
     */
    public static Connection getConnection(String driver, String url, String username, String password) {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.error("数据库连接异常：" + url);
        }
        return null;
    }

    private static void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        if (parameters != null) {
            for (int i = 0, size = parameters.size(); i < size; ++i) {
                Object param = parameters.get(i);
                stmt.setObject(i + 1, param);
            }
        }
    }

    public static List<Map<String, Object>> queryForList(Connection conn, String sql, List<Object> parameters)
            throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            rs = stmt.executeQuery();

            ResultSetMetaData rsMeta = rs.getMetaData();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<String, Object>();

                for (int i = 0, size = rsMeta.getColumnCount(); i < size; ++i) {
                    String columnName = rsMeta.getColumnLabel(i + 1);
                    Object value = getValueByType(rs, rsMeta.getColumnType(i + 1), rsMeta.getColumnLabel(i + 1));
                    row.put(columnName, value);
                }

                rows.add(row);
            }
        } finally {
            JdbcUtils.close(rs);
            JdbcUtils.close(stmt);
        }

        return rows;
    }


    /**
     * @Method : queryForList
     * @Description : 查询SQL返回对象列表
     * @param sql :
     * @param connection :
     * @return : java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:33:12
     */
    public static List<Map<String, Object>> queryForList( Connection connection,String sql){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        PreparedStatement ps = null;
        ResultSetMetaData rsmd = null;
        ResultSet rs = null;
        int columns;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            rsmd = rs.getMetaData();
            columns = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < columns; i++) {
                    map.put(rsmd.getColumnLabel(i + 1), getValueByType(rs, rsmd.getColumnType(i + 1), rsmd.getColumnLabel(i + 1)));
                }
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(ps);
        }
        return list;
    }

    /**
     * @Method : insertBatch
     * @Description : 批量插入数据
     * @Param connection :
     * @Param sql :
     * @Param dataList :
     * @Param keys :
     * @ReturnType : void
     * @Author : liuyang
     * @CreateDate : 2017-09-15 星期五 13:38:18
     */
    public static void insertBatch(Connection connection, String sql, List<Map<String, Object>> dataList, String[] keys) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            int count = 0;
            for (Map<String, Object> data : dataList) {
                for (int i = 0; i < keys.length; i++) {
                    String value = (String) data.get(keys[i]);
                    ps.setString(i + 1, value);
                }
                ps.addBatch();
                count++;
                if (count % 500 == 0) {//数量达到500后提交一次
                    ps.executeBatch();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();//批量更新
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
    }

    /**
     * @Method : count
     * @Description : select count查询获取数据数量
     * @param sql :
     * @param connection :
     * @return : int
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:37:45
     */
    public static int count(String sql, Connection connection) {
        int size = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                size = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(ps);
        }
        return size;
    }

    /**
     * @Method : getValueByType
     * @Description : 将执行的结果集进行类型转化选择
     * @param rs :
     * @param type :
     * @param name :
     * @Return : java.lang.Object
     * @Author : liuyang
     * @CreateDate : 2017-08-18 星期五 22:04:54
     */
    private static Object getValueByType(ResultSet rs, int type, String name) throws SQLException {

        switch (type) {
            case Types.NUMERIC:
                return rs.getLong(name);
            case Types.VARCHAR:
                return rs.getString(name);
            case Types.DATE:
                return rs.getDate(name);
            case Types.TIMESTAMP:
                return rs.getTimestamp(name).toString().substring(0, rs.getTimestamp(name).toString().length() - 2);
            case Types.INTEGER:
                return rs.getInt(name);
            case Types.DOUBLE:
                return rs.getDouble(name);
            case Types.FLOAT:
                return rs.getFloat(name);
            case Types.BIGINT:
                return rs.getLong(name);
            default:
                return rs.getObject(name);
        }
    }

    public static void close(Connection x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            log.debug("close connection error", e);
        }
    }

    public static void close(Statement x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            log.debug("close statement error", e);
        }
    }

    public static void close(ResultSet x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            log.debug("close result set error", e);
        }
    }
}
