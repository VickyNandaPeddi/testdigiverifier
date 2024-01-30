package com.aashdit.digiverifier.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
	private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);
	
	public static Date getLastDate(int days) {
		LocalDate today = LocalDate.now() ;
		LocalDate date = today.minus( Period.ofDays( days ) ) ;
		// Getting system timezone
		ZoneId systemTimeZone = ZoneId.systemDefault();
		
		// converting LocalDateTime to ZonedDateTime with the system timezone
		ZonedDateTime zonedDateTime = date.atStartOfDay(systemTimeZone);
		
		// converting ZonedDateTime to Date using Date.from() and ZonedDateTime.toInstant()
		Date utilDate = Date.from(zonedDateTime.toInstant());
		return utilDate;
	  }
	
	
	public static Date getDate(String doj,String pattern) {
		SimpleDateFormat sdfp = new SimpleDateFormat(pattern, Locale.ENGLISH);
		try{
			return sdfp.parse(doj);
		}catch(ParseException e){
			return null;
		}
	}
	
	public static String convertToString(ZonedDateTime zonedDateTime){
		try{
			DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
			return zonedDateTime.format(sdf);
		}catch(DateTimeException | NullPointerException e){
			logger.error(e.getMessage());
			return null;
		}
	}
	
	public static long differenceInMonths(Date date1,Date date2){
		
		
		Calendar sDate = Calendar.getInstance();
		Calendar eDate = Calendar.getInstance();
		if(date2.before(date1)){
			sDate.setTime(date1);
			eDate.setTime(date2);
		}else{
			sDate.setTime(date2);
			eDate.setTime(date1);
		}
		long remainingDays = Math.round((float) (eDate.getTimeInMillis() - sDate.getTimeInMillis()) / (24 * 60 * 60 * 1000));
		return Math.abs(remainingDays / 30);
	}	
	
	public static DateDifference getPreodDifference(Date date1, Date date2) {
		
		LocalDate localdate1 =date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate localdate2 =date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		Period period =Period.between(localdate1, localdate2);
		int years= period.getYears();
		int months=period.getMonths();
		int days=period.getDays();
		if(days>=30) {
			months++;
			days=0;
		}
		if(years==0 && months==0) {
			days=days+1;
		}
		
		return new DateDifference(years, months, days);
	}

}
