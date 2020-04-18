package org.easysql.info.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.easysql.info.constraint.ConstraintType;

@Data@AllArgsConstructor@NoArgsConstructor
public class ForeignKeyInfo {
    String fromTable;
    String toTable;
    String fromColumn;
    String toColumn;
    ConstraintType type;
    String name;
}
