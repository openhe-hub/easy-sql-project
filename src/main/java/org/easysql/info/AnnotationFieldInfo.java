package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @program: EasySqlProject
 * @description a bean to describe fields,foreign keys and indexes <br>
 * used in annotation configuration
 * @author: he
 * @create: 2020-04-08 16:54
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnotationFieldInfo {
    private String fieldName;
    private String fieldType;
    private String columnName;
    private String columnType;
    private ConstraintType[] constraints;
    private ForeignKeyInfo[] foreignKeyInfos;
    private IndexInfo[] indexInfos;
    private JoinInfo[] joinInfos;
}
