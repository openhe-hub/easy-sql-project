package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class Join {
    private String from_class_name;
    private String from_field;
    private String to_class;
    private ConstraintType type;
    private ConstraintType form;
    private String[] point;
    private String condition;
}
