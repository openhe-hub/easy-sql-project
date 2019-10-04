package org.easysql.helper;

import lombok.Getter;
import lombok.Setter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import java.io.*;

public class XmlHelper {
    @Setter@Getter
    public static String CONFIG_PATH="";
    @Getter
    public static final String CONFIG_FILE_TYPE=".xml";

    public static Element getRootElement(String config_name){
        Element element=null;
        SAXReader saxReader=new SAXReader();
        File file=new File(CONFIG_PATH+config_name+CONFIG_FILE_TYPE);
        try {
            Document document=saxReader.read(file);
           element=document.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        if (element!=null){
            return element;
        }
        else {
            System.out.println("config name error!");
            return null;
        }
    }
}
