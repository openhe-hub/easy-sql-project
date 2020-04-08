package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class FieldInfo {
    private String fieldName;
    private String fieldType;
    private String columnName;
    private String columnType;
    private ConstraintType[] constraints;

    public FieldInfo(String[] infos,ConstraintType[] constraints){
        this(infos[0],infos[1],infos[2],infos[3],constraints);
    }
}
