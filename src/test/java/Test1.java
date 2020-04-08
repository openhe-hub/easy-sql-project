import org.easysql.helper.Configuration;
import org.easysql.plugin.annotationHelper.BeanAnnotationAnalyzer;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-28 16:00
 **/
public class Test1 {
    @Test
    public void test1() {
        Configuration.autoConfigure(Test1.class);
        BeanAnnotationAnalyzer analyzer=new BeanAnnotationAnalyzer();
        analyzer.analyzeBeanAnnotation();
    }

    @Test
    public void test2(){
        System.out.println("\\"+File.separator);
    }
}
