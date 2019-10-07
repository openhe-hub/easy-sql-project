package org.easysql.info;

import lombok.Getter;

public enum ConstraintType {
    PRIMARY_KEY("primary key"),
    AUTO_INCREMENT("auto_increment"),
    NOT_NULL("not null"),
    NULL("null"),
    UNIQUE("unique"),
    MANY_TO_ONE("many_to_one"),
    ONE_TO_MANY("one_to_many"),
    ONE_TO_ONE("one_to_one");

    @Getter
    private String constraint_type;

    ConstraintType(String constraint_type) {
        this.constraint_type=constraint_type;
    }

    public static ConstraintType fromConstraintType(String str){
        for (ConstraintType type:ConstraintType.values()){
            String type_str=type.getConstraint_type();
            if (type_str.equals(str)){
                return type;
            }
        }
        System.out.println("error:constraint("+str+") not exist.Please check your mapping.xml");
        return null;
    }


}
