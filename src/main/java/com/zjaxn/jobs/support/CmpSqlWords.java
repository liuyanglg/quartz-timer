package com.zjaxn.jobs.support;


/**
 * @Package : com.aisino.mysql.constants
 * @Class : CmpSqlWords
 * @Description : 拼装的SQL查询语句
 * @Author : liuyang
 * @CreateDate : 2017-08-18 星期五 22:17:14
 * @Version : V1.0.0
 * @Copyright : 2017 liuyang Inc. All rights reserved.
 */
public class CmpSqlWords {

//    查询审核库新增数据量
//    SELECT  COUNT(*)  FROM  tb_cmp_card_audit  LEFT  JOIN  tb_cmp_card  ON  tb_cmp_card_audit.code  =  tb_cmp_card.code  WHERE  tb_cmp_card_audit.code !='' AND  tb_cmp_card_audit.createtime  >=  DATE_SUB(NOW(),INTERVAL 1 DAY)  ;

//    查询审核库新增数据
//    SELECT  tb_cmp_card_audit.code  AS  code  ,  tb_cmp_card_audit.taxid  AS  taxid  ,  tb_cmp_card.taxid  AS  taxid_alias  FROM  tb_cmp_card_audit  LEFT  JOIN  tb_cmp_card  ON  tb_cmp_card_audit.code  =  tb_cmp_card.code  WHERE  tb_cmp_card_audit.code !='' AND  tb_cmp_card_audit.createtime  >=  DATE_SUB(NOW(),INTERVAL 1 DAY)  LIMIT 0 , 500 ;

//    逐条查询审核库新增数据与用户中心一天前的数据的关系
//    SELECT 'Y76292' AS  code  , '201708041123Y76292' AS  taxid  ,  ucenter_user_service.c_serviceid  AS  serviceid  FROM  ucenter_user_service  WHERE  ucenter_user_service.c_taxnum  = '201708041123Y76292' AND  ucenter_user_service.c_serviceid  IS NOT NULL  AND  ucenter_user_service.c_taxnum !='' AND  ucenter_user_service.c_serviceid !='' AND  ucenter_user_service.dt_adddate  <  DATE_SUB(NOW(),INTERVAL 1 DAY)  ;

//    将查询出的审核库新增数据与用户中心一天前的数据的关系插入到关系表
//    INSERT INTO  tb_code_taxid_serviceid ( code  ,  taxid  ,  serviceid ) VALUE(? , ? , ?) ;

//    查询用户中心新增数据量
//    SELECT  COUNT(*)  FROM  ucenter_user_service  WHERE  ucenter_user_service.c_taxnum  IS NOT NULL  AND  ucenter_user_service.c_serviceid  IS NOT NULL  AND  ucenter_user_service.c_taxnum !='' AND  ucenter_user_service.c_serviceid !='' AND  ucenter_user_service.dt_adddate  >=  DATE_SUB(NOW(),INTERVAL 1 DAY)  ;

//    查询用户中心新增数据
//    SELECT  ucenter_user_service.c_taxnum  AS  taxid  ,  ucenter_user_service.c_serviceid  AS  serviceid  FROM  ucenter_user_service  WHERE  ucenter_user_service.c_serviceid  IS NOT NULL  AND  ucenter_user_service.c_taxnum  IS NOT NULL  AND  ucenter_user_service.c_serviceid !='' AND  ucenter_user_service.c_taxnum !='' AND  ucenter_user_service.dt_adddate  >=  DATE_SUB(NOW(),INTERVAL 1 DAY)  LIMIT 0 , 500 ;

//    查询用户中心新增数据量与审核库数据的关系，并将关系插入到关系表当中
//    INSERT INTO  tb_code_taxid_serviceid ( code  ,  taxid  ,  serviceid )( SELECT  DISTINCT  tb_cmp_card_audit.code  AS  code  ,  tb_cmp_card_audit.taxid  AS  taxid  , ? AS  serviceid  FROM  tb_cmp_card_audit  LEFT  JOIN  tb_cmp_card  ON  tb_cmp_card_audit.code  =  tb_cmp_card.code  WHERE  tb_cmp_card.taxid  = ? OR ( tb_cmp_card_audit.taxid  = ? AND  tb_cmp_card_audit.code  IS NULL )) ;

    public final static String TB_M = " tb_cmp_card ";
    public final static String TB_A = " tb_cmp_card_audit ";
    public final static String TB_U = " ucenter_user_service ";
    public final static String TB_R = " tb_code_taxid_serviceid ";
    public final static String SELECT = " SELECT ";
    public final static String DISTINCT = " DISTINCT ";
    public final static String COUNT = " COUNT(*) ";
    public final static String WHERE = " WHERE ";
    public final static String AND = " AND ";
    public final static String OR = " OR ";
    public final static String FROM = " FROM ";
    public final static String INSERT_INTO = " INSERT INTO ";
    public final static String VALUE = " VALUE";
    public final static String JOIN = " JOIN ";
    public final static String LEFT = " LEFT ";
    public final static String ON = " ON ";
    public final static String AS = " AS ";
    public final static String LIMIT = " LIMIT ";
    public final static String CODE = " code ";
    public final static String TAX_ID = " taxid ";
    public final static String TAX_ID_ALIAS = " taxid_alias ";
    public final static String SERVICE_ID = " serviceid ";
    public final static String TB_A_CODE = " tb_cmp_card_audit.code ";
    public final static String TB_A_TAX_ID = " tb_cmp_card_audit.taxid ";
    public final static String TB_A_CREATE_TIME = " tb_cmp_card_audit.createtime ";
    public final static String TB_M_CODE = " tb_cmp_card.code ";
    public final static String TB_M_TAX_ID = " tb_cmp_card.taxid ";
    public final static String TB_U_SERVICE_ID = " ucenter_user_service.c_serviceid ";
    public final static String TB_U_TAX_NUM = " ucenter_user_service.c_taxnum ";
    public final static String TB_U_ADD_DATE = " ucenter_user_service.dt_adddate ";
    public final static String DATE_SUB = " DATE_SUB(NOW(),INTERVAL ? DAY) ";
    public final static String SPLIT = " , ";
    public final static char PLACEHOLDER = '?';
    public final static String IS_NOT_NULL = " IS NOT NULL ";
    public final static String IS_NULL = " IS NULL ";
    public final static String NULL_STRING = "''";
    public final static String END = " ; ";

}
