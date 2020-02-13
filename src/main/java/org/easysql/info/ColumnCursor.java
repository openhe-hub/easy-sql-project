package org.easysql.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnCursor {
    int start;//start(inclusive)
    int end;//end(inclusive)
    String class_name;
    ArrayList<String> fields;
}

