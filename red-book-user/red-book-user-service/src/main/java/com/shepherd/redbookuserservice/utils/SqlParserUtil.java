package com.shepherd.redbookuserservice.utils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.sql.SQLSyntaxErrorException;
import java.util.List;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/9/2 13:59
 */
@Slf4j
public class SqlParserUtil {


    public static void parserSql(String sql){
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            Select select = (Select) stmt;
            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect) selectBody;
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            FromItem fromItem = plainSelect.getFromItem();
//            for(SelectItem selectItem : selectItems) {
//                if ()
//            }
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
