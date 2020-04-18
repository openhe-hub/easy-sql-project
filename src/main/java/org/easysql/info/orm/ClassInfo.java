package org.easysql.info.orm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class ClassInfo {
    private LinkedHashMap<String,String[]> classInfo;
    private LinkedHashMap<String, FieldInfo> fieldInfo;
    private LinkedHashMap<String,FieldInfo> columnInfo;
    private IdInfo idInfo;
    private ArrayList<ForeignKeyInfo> foreignKeyInfo;
    private ArrayList<IndexInfo> indexInfos;
    private LinkedHashMap<String, JoinInfo> joinInfo;
}
