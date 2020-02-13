package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
public class IdInfo extends FieldInfo{
   private String PrimaryKeyType;//is auto_increment

    public IdInfo(String[] finishedInfo,ConstraintType[] constraints, String primaryKeyType){
        super(finishedInfo, constraints);
        this.PrimaryKeyType = primaryKeyType;
    }
}
