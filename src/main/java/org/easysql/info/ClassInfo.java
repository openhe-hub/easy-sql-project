package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Data@AllArgsConstructor@NoArgsConstructor
public class ClassInfo {
    private LinkedHashMap<String,String[]> class_info;
    private LinkedHashMap<String, FieldInfo> field_infos;
    private LinkedHashMap<String,FieldInfo> column_infos;
    private IdInfo idInfo;
    private ArrayList<ForeignKeyInfo> foreignKeyInfos;
    private ArrayList<IndexInfo> indexInfos;
    private LinkedHashMap<String,Join> joins;
}
