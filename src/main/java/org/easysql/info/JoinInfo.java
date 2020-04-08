package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class JoinInfo {
    private String fromClassName;
    private String fromField;
    private String toClass;
    private ConstraintType type;
    private ConstraintType form;
    private String[] point;
    private String condition;
}
