package com.zjaxn.jobs.support;

import com.base.BaseTest;
import com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JskpJdbcUtilTest extends BaseTest {
    @Test
    public void executeQuery() throws Exception {
        String sql="SELECT *  FROM mongo_complany LIMIT ?,?;";
        try {
            List<Object> params = new ArrayList<Object>();
            Integer offset=0;
            Integer pageSize=10;
            params.add(offset);
            params.add(pageSize);
            List<Map<String, Object>>list= JskpJdbcUtil.executeQuery(ConnectionFactory.getConnection("dataserver"), sql,params);
            System.out.println(list);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}