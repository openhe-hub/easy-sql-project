package org.easysql.info;

import lombok.Getter;

public enum ConstraintType {
    PRIMARY_KEY("primary key"),
    AUTO_INCREMENT("auto_increment"),
    NOT_NULL("not null"),
    NULL("null"),
    UNIQUE("unique");
    @Getter
    private String constraint_type;

    ConstraintType(String constraint_type) {
        this.constraint_type=constraint_type;
    }

    public static ConstraintType fromConstraintType(String str){
        for (ConstraintType type:ConstraintType.values()){
            if (type.getConstraint_type().equals(str)){
                return type;
            }
        }
        System.out.println("error:constraint("+str+") not exist.Please check your student_mapping.xml");
        return null;
    }


}
