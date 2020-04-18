package org.easysql.info.orm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.easysql.info.constraint.ConstraintType;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
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
