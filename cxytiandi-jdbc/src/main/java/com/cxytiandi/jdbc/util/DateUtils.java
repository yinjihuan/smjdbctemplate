package com.cxytiandi.jdbc.util;


import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	public static final SafeSimpleDateFormat YYYY_MM_DD = new SafeSimpleDateFormat(
			"yyyy-MM-dd");
	public static final SafeSimpleDateFormat YYYY_MM_DD_HH = new SafeSimpleDateFormat(
			"yyyy-MM-dd HH");
	public static final SafeSimpleDateFormat YYYY_MM_DD_HH_MI = new SafeSimpleDateFormat(
			"yyyy-MM-dd HH:mm");
	public static final SafeSimpleDateFormat YYYY_MM_DD_HH_MI_SS = new SafeSimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public static final SafeSimpleDateFormat DEFAULT = new SafeSimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	/**
	 * 根据pattern得到SafeSimpleDateFormat
	 * 
	 * @CreateDate 2012-8-13 下午12:36:19
	 * @param pattern
	 * @return
	 */
	public static SafeSimpleDateFormat getDateFormat(String pattern) {
		if ("yyyy-MM-dd".equals(pattern)) {
			return YYYY_MM_DD;
		} else if ("yyyy-MM-dd HH".equals(pattern)) {
			return YYYY_MM_DD_HH;
		} else if ("yyyy-MM-dd HH:mm".equals(pattern)) {
			return YYYY_MM_DD_HH_MI;
		} else if ("yyyy-MM-dd HH:mm:ss".equals(pattern)) {
			return YYYY_MM_DD_HH_MI_SS;
		} else {
			return new SafeSimpleDateFormat(pattern);
		}
	}

	public static String date2Str(Date date) {
		return date2Str(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 转换日期为自定义的格式字符串
	 * 
	 * @param date
	 * @param pattern
	 * @return 格式化的日期字符串
	 */
	public static String date2Str(Date date, String pattern) {
		if (null == date) {
			return null;
		}
		String initDate = YYYY_MM_DD.format(new Date(0));
		if (initDate.equals(YYYY_MM_DD.format(date))) {
			return "";
		}
		return getDateFormat(pattern).format(date);
	}

	public static Date str2Date(String date, String pattern)
			throws ParseException {
		return getDateFormat(pattern).parse(date);
	}

	/**
	 * 根据传入的日期字符串自动转换为日期对象 <b>注意：只支持DateFormatUtils类中定义的几个常量字段格式</b>
	 * 
	 * @param date
	 * @return 根据字符串转换后的日期
	 * @throws ParseException
	 */
	public static Date str2Date(String date) throws ParseException {
		if (date.length() < 12) {
			return YYYY_MM_DD.parse(date);
		} else if (date.length() < 15) {
			return YYYY_MM_DD_HH.parse(date);
		} else if (date.length() < 18) {
			return YYYY_MM_DD_HH_MI.parse(date);
		} else {
			return YYYY_MM_DD_HH_MI_SS.parse(date);
		}
	}

	/**
	 * 得到一天的开始时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDayBegin(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 得到一天的结束时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDayEnd(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal.getTime();
	}
}
