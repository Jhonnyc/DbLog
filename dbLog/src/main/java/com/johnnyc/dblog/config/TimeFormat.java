package com.johnnyc.dblog.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum TimeFormat {
	// yyyy/MM/dd HH:mm:ss.SSS MM/dd/yyyy hh:mm a
	MM_DD_YYYY("MM/dd/yyyy"),
	MM_DD_YYYY__HH_mm("MM/dd/yyyy HH:mm"),
	MM_DD_YYYY__hh_mm_a("MM/dd/yyyy hh:mm a"),
	MM_DD_YYYY__HH_mm_ss("MM/dd/yyyy HH:mm:ss"),
	MM_DD_YYYY__hh_mm_ss_a("MM/dd/yyyy hh:mm:ss a"),
	MM_DD_YYYY__HH_mm_ss_SSS("MM/dd/yyyy HH:mm:ss:SSS"),
	MM_DD_YYYY__hh_mm_ss_SSS_a("MM/dd/yyyy hh:mm:ss:SSS a"),
	DD_MM_YYYY("dd/MM/yyyy"),
	DD_MM_YYYY__HH_mm("dd/MM/yyyy HH:mm"),
	DD_MM_YYYY__hh_mm_a("dd/MM/yyyy hh:mm a"),
	DD_MM_YYYY__HH_mm_ss("dd/MM/yyyy HH:mm:ss"),
	DD_MM_YYYY__hh_mm_ss_a("dd/MM/yyyy hh:mm:ss a"),
	DD_MM_YYYY__HH_mm_ss_SSS("dd/MM/yyyy HH:mm:ss:SSS"),
	DD_MM_YYYY__hh_mm_ss_SSS_a("dd/MM/yyyy hh:mm:ss:SSS a");
	
	// Enum Variables
	private String mName;
	private static final Map<String, TimeFormat> mValues = new HashMap<String, TimeFormat>();
	
	private TimeFormat(String value) {
		mName = value;
	}
	
	static {
		mValues.put(MM_DD_YYYY.toString().toLowerCase(Locale.getDefault()), MM_DD_YYYY);
    }
	
	@Override
	public String toString() {
		return mName;
	}
	
	public TimeFormat fromName(String name) throws NullPointerException {
		if(name == null || name.trim().length() < 1) {
			throw new NullPointerException("The name argument cannot be null");
		}
		return mValues.get(name.toLowerCase(Locale.getDefault()));
	}
}
