import org.easysql.bean.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-08 17:08
 **/
public class Test2 {
    @Test
    public void Test1() {
        Class<?> user= User.class;
        for (Field field : user.getDeclaredFields()) {
            System.out.println(field.getType().getName());
        }
    }
}
