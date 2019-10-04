package org.easysql.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class Student {
    private long id;
    private String name;
    private double mark;

    public Student(String name, double mark) {
        this.name = name;
        this.mark = mark;
    }
}
