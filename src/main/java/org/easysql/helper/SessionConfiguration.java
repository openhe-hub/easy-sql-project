package org.easysql.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionConfiguration{
    private String configXmlName;
    private String TableName;
    private String ClassName;
}
