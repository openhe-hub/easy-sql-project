package org.easysql.annotation.pojo;

import java.lang.annotation.*;

/**
 * use annotation to configure bean(pojo) in EasySql,
 * apply Convention Over Configuration theory,differ from
 * {@code EasySqlPojo.CLASS}.
 * <p>
 *     The following can be generated:
 *     <ul>
 *         <li>table name </li>
 *         <li>column name an type</li>
 *         <li>@Column annotation</li>
 *         <li>@id annotation(field name must contain 'id' to be detected)</li>
 *     </ul>
 * </p>
 * <p>To use customized configuration,you can extends
 * {@code PojoInfoGenerator.CLASS}</p>
 * @description smart pojo annotation configuration
 * @author  he
 * @create: 2020-04-07 14:26
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EasySqlSmartPojo {
    String tableName() default "";
    String sqlFile() default "";
}
