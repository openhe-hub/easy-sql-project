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

    public FieldInfo(String[] finishedInfo,ConstraintType[] constraints){
        this(finishedInfo[0],finishedInfo[1],finishedInfo[2],finishedInfo[3],constraints);
    }
}
