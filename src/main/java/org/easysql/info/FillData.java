package org.easysql.info;

import java.util.ArrayList;

/**
 * @program: EasySqlProject
 * @description
 * @author: he
 * @create: 2020-03-21 13:08
 **/
public class FillData {
    private ArrayList<String> params;

    public FillData(int i) {
        this();
        add(i);
    }

    public FillData(long i) {
        this();
        add(i);
    }

    public FillData(double i) {
        this();
        add(i);
    }

    public FillData(String str) {
        this();
        add(str);
    }

    public FillData(char c) {
        this();
        add(c);
    }

    public FillData(ArrayList<Object> objects){
        this();
        add(objects);
    }

    public FillData() {
        params = new ArrayList<>();
    }

    public FillData add(int i) {
        params.add(i+"");
        return this;
    }

    public FillData add(long i) {
        params.add(i+"");
        return this;
    }

    public FillData add(double i) {
        params.add(i+"");
        return this;
    }

    public FillData add(char c) {
        params.add(c+"");
        return this;
    }

    public FillData add(String str) {
        params.add(str);
        return this;
    }

    public FillData add(ArrayList<Object> objects){
        params.add(objects+"");
        return this;
    }

    public ArrayList<String> getParamList() {
        return params;
    }
}
