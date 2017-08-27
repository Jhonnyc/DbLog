package com.johnnyc.dblog.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.johnnyc.dblog.config.TimeFormat;

public class Utils {
	
	public static final String NEW_LINE = System.getProperty("line.separator");

	public static String getStringDateFromEpochTime(long epochTime, TimeFormat format) {
		Date date = new Date(epochTime);
		SimpleDateFormat formatter = new SimpleDateFormat(format.toString(), Locale.US);
		String formattedTime = formatter.format(date);
		return formattedTime;
	}
	
	public static String getStackTraceAsString(Throwable exception) {
		String stacktraceAsString = null;
		StackTraceElement[] elements = exception.getStackTrace();
		int length = elements.length;
		if(elements != null && length > 0) {
			stacktraceAsString = elements[0].toString();
			if(length > 1) {
				for(int i = 1; i < length; i ++) {
					stacktraceAsString += String.format(", %s", elements[length - 1].toString());
				}
			}
		}

		return stacktraceAsString;
	}
}
