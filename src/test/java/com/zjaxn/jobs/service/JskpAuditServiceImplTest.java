package com.zjaxn.jobs.service;

import com.base.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class JskpAuditServiceImplTest extends BaseTest {

    @Autowired
    @Qualifier("jskpAuditService")
    private JskpAuditService jskpAuditService;

    @Test
    public void checkICBC() throws Exception {
//        JskpAuditServiceImpl auditService = new JskpAuditServiceImpl();
        jskpAuditService.checkICBC("320481600503025", "溧阳市溧城空维图文工作室");
    }

}