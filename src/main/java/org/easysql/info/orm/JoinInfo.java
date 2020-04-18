package org.easysql.info.orm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.easysql.info.constraint.ConstraintType;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class JoinInfo {
    private String fromClass;
    private String fromField;
    private String toClass;
    private ConstraintType type;
    private ConstraintType form;
    private String[] point;
    private String condition;
}
