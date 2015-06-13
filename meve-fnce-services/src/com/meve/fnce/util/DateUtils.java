package com.meve.fnce.util;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	
    protected static Date getDate(String date, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(date);
    }
        
    protected static Date getDate(Date date, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(sdf.format(date));
    }    
     
    public static String convertLocalTimeToUTC(Date localDate) throws ParseException {
        SimpleDateFormat utcFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");   
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateUTCAsString = utcFormat.format(getDate(localDate,"yyyy-MM-dd HH:mm:ss z"));   
        return dateUTCAsString;   
    }   

    public static String convertLocalTimeToUTC(String localDate) throws ParseException {
        Date fecha= getDate(localDate,"yyyy-MM-dd HH:mm:ss");  
        SimpleDateFormat utcFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");   
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateUTCAsString = utcFormat.format(getDate(fecha,"yyyy-MM-dd HH:mm:ss z"));   
        return dateUTCAsString;   
    } 	

}
