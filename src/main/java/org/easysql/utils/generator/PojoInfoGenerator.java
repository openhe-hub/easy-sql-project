package org.easysql.utils.generator;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-25 17:33
 **/
public interface PojoInfoGenerator {
    String generateTable(String pojoClassName);
    String generateSqlFile(String pojoClassName);
    String generateColumnName(String fieldName);
}
