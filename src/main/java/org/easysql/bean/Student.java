package org.easysql.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor@NoArgsConstructor
public class Student {
    private long id;
    private String name;
    private double mark;
    private long teacher_id;

    public Student(String name, double mark, long teacher_id) {
        this.name = name;
        this.mark = mark;
        this.teacher_id = teacher_id;
    }
}
