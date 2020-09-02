package com.shepherd.redbookuserservice;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.shepherd.redbookuserservice.exception.BusinessException;
import com.shepherd.redbookuserservice.utils.SqlParserUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.shepherd.redbookuserservice.utils.SqlParserUtil.parser;
import static com.shepherd.redbookuserservice.utils.SqlParserUtil.parserSql;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/9/1 19:07
 */
@RunWith(SpringRunner.class)
@Slf4j
public class SqlParserTest {

    @Test
    public void test() {
        String sql = "select a.id,a.name,b.project_name\n" +
                "from atlas a \n" +
                "left join \n" +
                "(select id,name as project_name from project) b\n" +
                "on a.project_id=b.id";

        try {
            SqlParserUtil.parserSql(sql);
//            Statement stmt = CCJSqlParserUtil.parse(sql);
//            Select select = (Select) stmt;
//            SelectBody selectBody = select.getSelectBody();
//            PlainSelect plainSelect = (PlainSelect) selectBody;
//
//            // first step check sql valid
//            for (SelectItem selectItem : ((PlainSelect) selectBody).getSelectItems()) {
//                if (selectItem instanceof AllColumns || selectItem instanceof AllTableColumns){
//                    throw new BusinessException("不符合sql开发规范");
//                }
//                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
//            }
//            //From分析
//            FromItem fromItem = plainSelect.getFromItem();
//            Table table = (Table) fromItem; // table.getAlias().getName(), table.getName()
//
//            // join分析
//            List<Join> joins = plainSelect.getJoins();
//            for (Join join : joins) {
//                FromItem rightItem = join.getRightItem();
//                Table table1 = (Table) rightItem; // (table.getAlias().getName(), table.getName());
//                int i=0;
//            }
        } catch (Exception e) {
            log.error("sql parser error:", e);


        }

    }

    @Test
    public void testDruid(){
        try {
            String sql = "SELECT\n" +
                    "\t_2.DISPLAY,\n" +
                    "CASE\n" +
                    "\t\t\n" +
                    "\t\tWHEN _4.DEVICE_NAME IS NULL THEN\n" +
                    "\t\t_1.DEVICE_ID ELSE CONCAT( _1.DEVICE_ID, '-', _4.DEVICE_NAME ) \n" +
                    "\tEND AS DEVICE_NAME,\n" +
                    "CASE\n" +
                    "\t\t\n" +
                    "\t\tWHEN _3.REAL_BEAT IS NULL THEN\n" +
                    "\t\t0 \n" +
                    "\t\tWHEN _3.DESIGN_BEAT / _3.REAL_BEAT IS NULL THEN\n" +
                    "\t\t1 ELSE _3.DESIGN_BEAT / _3.REAL_BEAT \n" +
                    "\tEND AS PCT \n" +
                    "FROM\n" +
                    "\t(\n" +
                    "\tSELECT\n" +
                    "\t\tT1.DEVICE_ID AS DEVICE_ID,\n" +
                    "\t\tSUM( T1.QUANTITY ) AS QUANT \n" +
                    "\tFROM\n" +
                    "\t\tfact_mdc_product_info T1\n" +
                    "\t\tINNER JOIN dim_unit_info T2 ON T1.UNITCODE = T2.UNITCODE \n" +
                    "\tWHERE\n" +
                    "\t\tT1.TRANS_DATE = '2019-06-29' \n" +
                    "\t\tAND T2.REGIONNAME IN ( '凌云西南' ) \n" +
                    "\tGROUP BY\n" +
                    "\t\tT1.DEVICE_ID \n" +
                    "\tORDER BY\n" +
                    "\t\tQUANT DESC \n" +
                    "\t\tLIMIT 4 \n" +
                    "\t) _1\n" +
                    "\tLEFT JOIN ( SELECT DEVICE_ID, DEVICE_NAME FROM fact_mdc_static GROUP BY DEVICE_ID ) _4 ON _4.DEVICE_ID = _1.DEVICE_ID\n" +
                    "\tCROSS JOIN dim_halfhour _2\n" +
                    "\tLEFT JOIN (\n" +
                    "\tSELECT DISTINCT\n" +
                    "\t\tT1.device_id,\n" +
                    "\t\tT1.BEGIN_TIME,\n" +
                    "\t\tT1.END_TIME,\n" +
                    "\t\tTIMESTAMPDIFF( SECOND, T1.BEGIN_TIME, T1.END_TIME ) / T1.QUANTITY AS real_beat,\n" +
                    "\t\tT3.DESIGN_BEAT \n" +
                    "\tFROM\n" +
                    "\t\tfact_mdc_product_info T1\n" +
                    "\t\tLEFT JOIN ( SELECT T2.PRODUCT_ID, AVG( T2.DESIGN_BEAT ) AS DESIGN_BEAT FROM fact_mdc_static T2 GROUP BY T2.product_id ) T3 ON T1.PRODUCT_ID = T3.product_id \n" +
                    "\tWHERE\n" +
                    "\t\tTRANS_DATE = '2019-06-29' \n" +
                    "\t) _3 ON _2.ts BETWEEN TIME( _3.BEGIN_TIME ) \n" +
                    "\tAND TIME( _3.end_time ) \n" +
                    "\tAND _1.DEVICE_ID = _3.DEVICE_ID;";
            String dbType = "mysql";
            System.out.println("原始SQL 为 ： "+sql);
            SQLSelectStatement statement = (SQLSelectStatement) parser(sql, dbType);
            SQLSelect select = statement.getSelect();
            SQLSelectQueryBlock query = (SQLSelectQueryBlock) select.getQuery();
            SQLExprTableSource tableSource = (SQLExprTableSource) query.getFrom();
            String tableName = tableSource.getExpr().toString();
            System.out.println("获取的表名为  tableName ：" + tableName);
            //修改表名为acct_1
            tableSource.setExpr("acct_1");
            System.out.println("修改表名后的SQL 为 ： [" + statement.toString() +"]");
        } catch(Exception e) {
            log.error("druid sql parser error:", e);
        }

    }

}
