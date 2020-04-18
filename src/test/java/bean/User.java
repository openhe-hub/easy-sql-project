package bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.easysql.annotation.pojo.Column;
import org.easysql.annotation.pojo.EasySqlPojo;
import org.easysql.annotation.pojo.Id;
import org.easysql.info.constraint.ConstraintType;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-08 13:08
 **/
@Data@AllArgsConstructor@NoArgsConstructor
@EasySqlPojo(tableName = "user")
public class User {
    @Id
    private String uid;
    @Column(constraintTypes = ConstraintType.UNIQUE)
    private String name;
    @Column
    private String password;
    @Column
    private int age;
}

/*@Data@AllArgsConstructor@NoArgsConstructor
@EasySqlSmartPojo
public class User {
    @Id
    private String uid;
    @Column(constraintTypes = ConstraintType.UNIQUE)
    private String name;
    private String password;
    private int age;
}*/
