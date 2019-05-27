package com.isssr.ticketing_system.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

//NB: classe usate per fare il parsing delle date con GregorianCalendar
public class ParseDate {
    public ParseDate() {
    }

    public static GregorianCalendar parseGregorianCalendar(String data){

        String[] splitStr = data.split("[/ :-]");


        int day = Integer.parseInt(splitStr[0]);
        int month = Integer.parseInt(splitStr[1]);
        int year = Integer.parseInt(splitStr[2]);
        int hour = 0, minute = 0, second = 0;
        if (splitStr.length > 3) {
            hour = Integer.parseInt(splitStr[3]);
            minute = Integer.parseInt(splitStr[4]);
            second = Integer.parseInt(splitStr[5]);
        }


        if(month==0)
            return new GregorianCalendar(year,Calendar.JANUARY,day,hour,minute,second);
        else if(month==1)
            return new GregorianCalendar(year,Calendar.FEBRUARY,day,hour,minute,second);
        else if(month==2)
            return new GregorianCalendar(year,Calendar.MARCH,day,hour,minute,second);
        else if(month==3)
            return new GregorianCalendar(year,Calendar.APRIL,day,hour,minute,second);
        else if(month==4)
            return new GregorianCalendar(year,Calendar.MAY,day,hour,minute,second);
        else if(month==5)
            return new GregorianCalendar(year,Calendar.JUNE,day,hour,minute,second);
        else if(month==6)
            return new GregorianCalendar(year,Calendar.JULY,day,hour,minute,second);
        else if(month==7)
            return new GregorianCalendar(year,Calendar.AUGUST,day,hour,minute,second);
        else if(month==8)
            return new GregorianCalendar(year,Calendar.SEPTEMBER,day,hour,minute,second);
        else if(month==9)
            return new GregorianCalendar(year,Calendar.OCTOBER,day,hour,minute,second);
        else if(month==10)
            return new GregorianCalendar(year,Calendar.NOVEMBER,day,hour,minute,second);
        else
            return new GregorianCalendar(year,Calendar.DECEMBER,day,hour,minute,second);

    }


    public static String gregorianCalendarToString(GregorianCalendar gregorianCalendar){

        return gregorianCalendar.get(Calendar.DATE)+ "/" + gregorianCalendar.get(Calendar.MONTH)+ "/" + gregorianCalendar.get(Calendar.YEAR);

    }

}
