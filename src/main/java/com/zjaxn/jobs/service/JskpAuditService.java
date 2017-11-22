package com.zjaxn.jobs.service;

import com.zjaxn.jobs.utils.model.JskpCardAudit;

import java.util.List;
import java.util.Map;


public interface JskpAuditService {
    List<Map<String, Object>> queryPage(int offset, int pageSize);

    int count();

    void pushRedis(List<Map<String, Object>> list);

    void pushBigDataRedis(List<Map<String, Object>> list);

    List<JskpCardAudit> popRedis(int batchSize);

    void auditData(List<JskpCardAudit> list);

    void setLastId(int id);

    int getLastId();

    int checkICBC(String taxid, String name);
}
