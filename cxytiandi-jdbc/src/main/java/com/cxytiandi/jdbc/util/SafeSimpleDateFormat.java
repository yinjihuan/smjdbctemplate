package com.cxytiandi.jdbc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * https://code.google.com/p/safe-simple-date-format/
 * This class implements a Thread-Safe (re-entrant) SimpleDateFormat class. It
 * does this by using a ThreadLocal that holds a Map, instead of the traditional
 * approach to hold the SimpleDateFormat in a ThreadLocal.
 * 
 * Each ThreadLocal holds a single HashMap containing SimpleDateFormats, keyed
 * by a String format (e.g. "yyyy/M/d", etc.), for each new SimpleDateFormat
 * instance that was created within the threads execution context.
 * 
 * @author John DeRegnaucourt
 */
public class SafeSimpleDateFormat {
	private final String _format;

	private static final ThreadLocal _dateFormats = new ThreadLocal() {
		public Object initialValue() {
			return new HashMap();
		}
	};

	@SuppressWarnings("unchecked")
	private SimpleDateFormat getDateFormat(String format) {
		Map<String, SimpleDateFormat> formatters = (Map) _dateFormats.get();
		SimpleDateFormat formatter = formatters.get(format);
		if (formatter == null) {
			formatter = new SimpleDateFormat(format);
			formatters.put(format, formatter);
		}
		return formatter;
	}

	public SafeSimpleDateFormat(String format) {
		_format = format;
	}

	public String format(Date date) {
		return getDateFormat(_format).format(date);
	}

	public String format(Object date) {
		return getDateFormat(_format).format(date);
	}

	public Date parse(String day) throws ParseException {
		return getDateFormat(_format).parse(day);
	}
}