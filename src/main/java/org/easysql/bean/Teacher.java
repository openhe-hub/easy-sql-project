package org.easysql.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data@AllArgsConstructor@NoArgsConstructor
public class Teacher {
    private long id;
    private String name;
    private ArrayList<Student> students;


    public Teacher(String name, ArrayList<Student> students) {

        this.name = name;
        this.students = students;
    }
}
