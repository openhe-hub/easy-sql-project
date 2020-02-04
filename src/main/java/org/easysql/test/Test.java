package org.easysql.test;

import org.easysql.bean.Student;
import org.easysql.helper.CommonValue;
import org.easysql.helper.Configuration;
import org.easysql.session.*;


public class Test {
    public static void main(String[] args)  {
        Configuration.configure(Test.class);
        Session<Student> studentSession=new Session<>("Student");
        studentSession.update(CommonValue.ADD_COLUMN+"birthday");
    }
}
