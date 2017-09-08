package com.johnnyc.dblog.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum LogLevel {
	VERBOSE("Verbose"),
	DEBUG("Debug"),
	INFO("Info"),
	WARNING("Warning"),
	ERROR("Error"),
	WTF("WTF"); // What a teriible failure - for cases that should never happen
	
	// Enum Variables
	private String mName;
	private static final Map<String, LogLevel> mValues = new HashMap<String, LogLevel>();
	private static List<LogLevel> mValuesList;
	
	private LogLevel(String value) {
		mName = value;
	}
	
	static {
		mValues.put(VERBOSE.toString().toLowerCase(), VERBOSE);
		mValues.put(DEBUG.toString().toLowerCase(), DEBUG);
		mValues.put(INFO.toString().toLowerCase(), INFO);
		mValues.put(WARNING.toString().toLowerCase(), WARNING);
		mValues.put(ERROR.toString().toLowerCase(), ERROR);
		mValuesList = Arrays.asList(values());
    }
	
	@Override
	public String toString() {
		return mName;
	}
	
	public LogLevel fromName(String name) throws NullPointerException {
		if(name == null || name.trim().length() < 1) {
			throw new NullPointerException("The name argument cannot be null");
		}
		return mValues.get(name.toLowerCase());
	}
	
	public static List<LogLevel> asList() {
		return mValuesList;
	}
}
