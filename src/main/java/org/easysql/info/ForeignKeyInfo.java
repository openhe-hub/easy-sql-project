package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class ForeignKeyInfo {
    String fromTable;
    String toTable;
    String fromColumn;
    String toColumn;
    ConstraintType type;
    String name;
}
