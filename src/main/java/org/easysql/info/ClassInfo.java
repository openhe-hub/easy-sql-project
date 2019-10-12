package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Data@AllArgsConstructor
public class ClassInfo {
    private LinkedHashMap<String,String[]> class_info;
    private LinkedHashMap<String, FieldInfo> field_infos;
    private IdInfo idInfo;
    private ArrayList<ForeignKeyInfo> foreignKeyInfos;
    private ArrayList<IndexInfo> indexInfos;
}
