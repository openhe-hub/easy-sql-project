package org.easysql.utils;

import java.io.*;
import java.util.regex.Matcher;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-21 14:48
 **/
public class SqlIdScanner {
    private String idFilePath;
    private File idFile;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String packageName;
    private String className;
    private final String idFileType=".java";

    private final String PKG_COMMAND="package ";
    private final String CLASS_DEFINITION_COMMAND="public final class ";
    private final String CLOZE_BRACKET="}";
    private final String OPEN_BRACKET="{";
    private final String ID_STRING_DEFINITION_COMMAND="public static final String ";
    private final String SEMICOLON=";";
    private final String BR="\n";

    public void startScan(){
       /* idFilePath=Configuration.getIdFile();*/
        String origin="org.easysql.info.R";
        getPackageName(origin);
        createFile(origin);
        try {
            idFile.createNewFile();
            writer = new BufferedWriter(new FileWriter(idFile));
            writer.write(PKG_COMMAND+packageName+SEMICOLON+BR);
            writer.write(BR);
            writer.write(CLASS_DEFINITION_COMMAND+className+OPEN_BRACKET+BR);
            writer.write(BR);
            writer.write(CLOZE_BRACKET+BR);
            writer.flush();
            writer=new BufferedWriter(new FileWriter(idFile));
            writer.write(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SqlIdScanner().startScan();
    }


    private void getPackageName(String origin){
        StringBuilder result= new StringBuilder();
        String[] strings=origin.split("\\.");
        for (int i=0;i<strings.length-1; i++){
            result.append(strings[i]).append(".");
        }
        className=strings[strings.length - 1];
        this.packageName=result.substring(0,result.length() - 1);
    }

    private void createFile(String origin){
        idFilePath="src"+File.separator+"main"+File.separator+"java"+File.separator;
        origin = origin.replaceAll("\\.",Matcher.quoteReplacement(File.separator));
        idFilePath+=origin;
        idFilePath+=idFileType;
        this.idFile=new File(idFilePath);
    }
}
