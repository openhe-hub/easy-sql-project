package bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.easysql.annotation.pojo.Column;
import org.easysql.annotation.pojo.EasySqlPojo;
import org.easysql.annotation.pojo.EasySqlSmartPojo;
import org.easysql.annotation.pojo.Id;
import org.easysql.info.constraint.ConstraintType;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-08 13:08
 **/
@Data@NoArgsConstructor@AllArgsConstructor@Builder
@EasySqlSmartPojo
public class User {
    private int uid;
    @Column(constraintTypes = ConstraintType.UNIQUE)
    private String name;
    private String password;
}

