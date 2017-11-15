package com.zjaxn.jobs.support;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class JskpJdbcUtil {

    public static void close(Connection x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            System.out.println("close connection error");
        }
    }

    public static void close(Statement x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            System.out.println("close statement error");
        }
    }

    public static void close(ResultSet x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            System.out.println("close result set error");
        }
    }

    private static void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        if (parameters == null) {
            return;
        }

        for (int i = 0, size = parameters.size(); i < size; ++i) {
            Object param = parameters.get(i);
            stmt.setObject(i + 1, param);
        }
    }

    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, Object... parameters)
            throws SQLException {
        return executeQuery(dataSource, sql, Arrays.asList(parameters));
    }

    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, List<Object> parameters)
            throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return executeQuery(conn, sql, parameters);
        } finally {
            close(conn);
        }
    }

    public static List<Map<String, Object>> executeQuery(Connection conn, String sql, List<Object> parameters)
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
                    Object value = rs.getObject(i + 1);
                    row.put(columnName, value);
                }

                rows.add(row);
            }
        } finally {
            close(rs);
            close(stmt);
        }

        return rows;
    }

    public static List<Map<String, Object>> queryPage(Connection conn, String sql, int offset, int pageSize) throws SQLException {
        List<Map<String, Object>> list = null;

        List<Object> params = new ArrayList<Object>();
        params.add(offset);
        params.add(pageSize);
        list = JskpJdbcUtil.executeQuery(conn, sql, params);
        return list;
    }

    public static int count(Connection connection, String sql) {
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
}
