package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Data@AllArgsConstructor@NoArgsConstructor
public class ClassInfo {
    private LinkedHashMap<String,String[]> classInfo;
    private LinkedHashMap<String, FieldInfo> fieldInfos;
    private LinkedHashMap<String,FieldInfo> columnInfos;
    private IdInfo idInfo;
    private ArrayList<ForeignKeyInfo> foreignKeyInfos;
    private ArrayList<IndexInfo> indexInfos;
    private LinkedHashMap<String, JoinInfo> joins;
}
