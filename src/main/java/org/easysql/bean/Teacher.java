package org.easysql.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class Teacher {
    private long id;
    private String name;

    public Teacher(String name) {
        this.name = name;
    }
}
