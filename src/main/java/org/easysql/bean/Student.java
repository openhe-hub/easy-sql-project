package org.easysql.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data@AllArgsConstructor@NoArgsConstructor@Builder
public class Student {
    private int id;
    private int student_id;
    private String name;
    private long teacher_id;
    private Date birthday;

    private Mark mark;
    private Teacher teacher;

    public Student(String name, long teacher_id,int student_id) {
        this.name = name;
        this.teacher_id = teacher_id;
        this.student_id=student_id;
    }

    public Student(Mark mark,Teacher teacher){
        this.mark=mark;
        this.teacher=teacher;
    }
}
