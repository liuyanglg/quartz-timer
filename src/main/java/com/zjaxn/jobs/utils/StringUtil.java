package com.zjaxn.jobs.utils;

import java.util.Date;

public class StringUtil {

    public static boolean isNumeric(String string) {
        if (string == null || string.length() == 0)
            return false;

        int l = string.length();
        for (int i = 0; i < l; i++) {
            if (!Character.isDigit(string.codePointAt(i)))
                return false;
        }
        return true;
    }

    public static boolean isWhitespace(int c){
        return c == ' ' || c == '\t' || c == '\n' || c == '\f' || c == '\r';
    }

    public static boolean isBlank(String string) {
        if (string == null || string.length() == 0)
            return true;

        int l = string.length();
        for (int i = 0; i < l; i++) {
            if (!isWhitespace(string.codePointAt(i)))
                return false;
        }
        return true;
    }

    public static String joinStringField4Json(String fieldName,String value){
        if(isBlank(value)){
            return "";
        }
        else{
            return "\"" + fieldName + "\":\"" + value +"\",";
        }
    }

    public static String joinIntegerField4Json(String fieldName,Integer value){
        if(value==null){
            return "";
        }
        else{
            return "\"" + fieldName + "\":\"" + value +"\",";
        }
    }

    public static String joinDateField4Json(String fieldName,Date value){
        if(value==null){
            return "";
        }
        else{
            return "\"" + fieldName + "\":\"" + DateUtil.format(value) +"\",";
        }
    }
}

