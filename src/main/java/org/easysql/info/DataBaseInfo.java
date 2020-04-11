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

@Data@AllArgsConstructor@NoArgsConstructor
public class DataBaseInfo {
    private String username;
    private String password;
    private String databaseName;
    private String driverClassName;
    private String url;
    private String urlParameters;
    private int maxActive=10;
    private int initialSize=10;
    private long maxWait=3000;
}
