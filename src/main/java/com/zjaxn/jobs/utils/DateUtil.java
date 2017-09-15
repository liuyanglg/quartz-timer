package com.zjaxn.jobs.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static String format(Date date){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	public static Date formatStringToDate(String date){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			return format.parse(date);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String format(String timastamp){
		Date date = new Date(Long.parseLong(timastamp));
		return format(date);
	}
}
