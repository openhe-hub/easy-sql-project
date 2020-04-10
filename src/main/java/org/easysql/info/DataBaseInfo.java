package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-04-10 12:26
 **/

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class DataBaseInfo {
    private String username;
    private String password;
    private String databaseName;
    private String driverClassName="com.mysql.cj.jdbc.Driver";
    private String url="jdbc:mysql://localhost:3306/";
    private String urlParameters="characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true";
    private int maxActive=10;
    private int initialSize=10;
    private long maxWait=3000;
}
