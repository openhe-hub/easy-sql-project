package org.easysql.info;

import lombok.Getter;
import org.easysql.helper.CommonValue;
import org.easysql.helper.Configuration;

public enum ConstraintType {

    PRIMARY_KEY("primary key"),
    AUTO_INCREMENT("auto_increment"),
    NOT_NULL("not null"),
    NULL("null"),
    UNIQUE("unique"),

    MANY_TO_ONE("many to one"),
    ONE_TO_MANY("one to many"),
    ONE_TO_ONE("one to one"),

    FULL_TEXT_INDEX("fulltext"),
    UNIQUE_INDEX("unique"),
    INDEX("index"),

    LEFT_JOIN("left join"),
    RIGHT_JOIN("right join"),
    INNER_JOIN("inner join");


    @Getter
    private String constraintType;

    ConstraintType(String constraintType) {
        this.constraintType = constraintType;
    }

    public static ConstraintType fromConstraintType(String str){
        for (ConstraintType type:ConstraintType.values()){
            String type_str=type.getConstraintType();
            if (type_str.equals(str)){
                return type;
            }
        }
        Configuration.createLogger(ConstraintType.class).
                error(CommonValue.ERROR +"constraint type("+str+") not exist.Please check your mapping.xml");
        return null;
    }
}
