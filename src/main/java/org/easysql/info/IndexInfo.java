package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class IndexInfo {
    private String fieldName;
    private String name;
    private ConstraintType type;
}
