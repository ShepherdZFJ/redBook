package com.shepherd.redbookuserservice.utils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.shepherd.redbookuserservice.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/9/2 13:59
 */
@Slf4j
public class SqlParserUtil {


    public static void parserSql(String sql){
        List< Map<String, Object> > list = new ArrayList<>();
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            Select select = (Select) stmt;
            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect) selectBody;

            // first step check sql valid
            for (SelectItem selectItem : ((PlainSelect) selectBody).getSelectItems()) {
                if (selectItem instanceof AllColumns || selectItem instanceof AllTableColumns){
                    throw new BusinessException("不符合sql开发规范");
                }
                Map<String, String> map = new HashMap<>();
                Map<String, Object> result = new HashMap<>();
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                Expression expression = selectExpressionItem.getExpression();
                Alias alias = selectExpressionItem.getAlias();
                if (expression instanceof Column) {
                    Column column = (Column) expression;
                    map.put("table", column.getTable().toString());
                    map.put("columnName", column.getColumnName());
                    if (alias != null) {
                        map.put("name",alias.getName());
                    }
                    result.put("column", map);
                } else {
                    String s = "="+expression.toString();
                    map.put("expression",s);
                    if (alias != null) {
                        map.put("name",alias.getName());
                    }
                    result.put("expression", map);
                }
                list.add(result);
            }
            //From分析
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem instanceof Table) {
                Table table = (Table) fromItem;
                Alias alias = table.getAlias();
                String name = table.getName();
                int i=0;
            }

            // join分析
            List<Join> joins = plainSelect.getJoins();
            for (Join join : joins) {
                FromItem rightItem = join.getRightItem();
                Table table1 = (Table) rightItem; // (table.getAlias().getName(), table.getName());
                int i=0;
            }
        } catch (Exception e) {
            log.error("sql parser error:", e);
        }
    }

    public static SQLStatement parser(String sql,String dbType) throws SQLSyntaxErrorException {
        List<SQLStatement> list = SQLUtils.parseStatements(sql, dbType);
        if (list.size() > 1) {
            throw new SQLSyntaxErrorException("MultiQueries is not supported,use single query instead ");
        }
        return list.get(0);
    }




}
