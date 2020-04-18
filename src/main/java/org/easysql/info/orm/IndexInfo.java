package org.easysql.info.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.easysql.info.constraint.ConstraintType;

@Data@AllArgsConstructor@NoArgsConstructor
public class IndexInfo {
    private String columnName;
    private String name;
    private ConstraintType type;
}
