package org.easysql.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@NoArgsConstructor@AllArgsConstructor
public class Mark {
    private int id;
    private double mark;
    private int student_id;
    private Student student;

    public Mark(double mark, int student_id, Student student) {
        this.mark = mark;
        this.student_id = student_id;
        this.student = student;
    }


}
