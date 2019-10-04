package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class FieldInfo {
    private String field_name;
    private String field_type;
    private String column_name;
    private String column_type;
    private ConstraintType[] constraints;

    public FieldInfo(String[] finished_info,ConstraintType[] constraints){
        this(finished_info[0],finished_info[1],finished_info[2],finished_info[3],constraints);
    }
}
