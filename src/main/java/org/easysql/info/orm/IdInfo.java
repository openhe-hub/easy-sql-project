package org.easysql.info.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.easysql.info.constraint.ConstraintType;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@AllArgsConstructor
public class IdInfo extends FieldInfo {
   private String PrimaryKeyType;//is auto_increment

    public IdInfo(String[] finishedInfo, ConstraintType[] constraints, String primaryKeyType){
        super(finishedInfo, constraints);
        this.PrimaryKeyType = primaryKeyType;
    }
}
