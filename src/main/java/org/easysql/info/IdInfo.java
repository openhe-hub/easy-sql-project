package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
public class IdInfo extends FieldInfo{
   private String pk_type;//is auto_increment

    public IdInfo(String[] finished_info,ConstraintType[] constraints, String pk_type){
        super(finished_info, constraints);
        this.pk_type = pk_type;
    }
}
