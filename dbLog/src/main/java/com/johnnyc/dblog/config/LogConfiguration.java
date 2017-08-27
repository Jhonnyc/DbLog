package com.johnnyc.dblog.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class LogConfiguration {

	// Class Variables
	private int mLogMaxLines;
	private String mLogFileName;
	private TimeFormat mTimeFormat;
	private List<LogLevel> mLogLevelList;
	private List<LogLevel> mStackTraceList;

	public LogConfiguration() {
		mLogMaxLines = 1000;
		mLogFileName = "OkLog.log";
		mTimeFormat = TimeFormat.DD_MM_YYYY__HH_mm_ss_SSS;
		mLogLevelList = LogLevel.asList();
		mStackTraceList = new ArrayList<LogLevel>();
		mStackTraceList.add(LogLevel.ERROR);
	}

	public int getSize() {
		return mLogMaxLines;
	}
	
	public void setSize(int size) throws IllegalArgumentException {
		if(size < 1) {
			throw new IllegalArgumentException("Size has to be a positive number");
		} else {
			mLogMaxLines = size;
		}
	}
	
	public String getLogFileName() {
		return mLogFileName;
	}

	public void setLogFileName(String logFileName) throws InvalidParameterException, IllegalArgumentException {
		if(logFileName == null || logFileName.trim().length() < 3 || !logFileName.contains(".")) {
			throw new InvalidParameterException("Log file name has to be in the form of <name>.[<log> or <txt>]");
		} else if(!logFileName.subSequence(logFileName.indexOf("."), logFileName.length()).equals("txt") &&
				!logFileName.subSequence(logFileName.indexOf("."), logFileName.length()).equals("log")) {
			throw new InvalidParameterException("Log file type can only be <log> or <txt>");
		} else {
			mLogFileName = logFileName;
		}
	}
	
	public TimeFormat getTimeFormat() {
		return mTimeFormat;
	}

	public void setTimeFormat(TimeFormat timeFormat) {
		mTimeFormat = timeFormat;
	}
	
	public List<LogLevel> getLevelsToLog() {
		return mLogLevelList;
	}
	
	public void setLevelsTolog(List<LogLevel> levelsTolog) throws IllegalArgumentException {
		if(levelsTolog.size() < 1) {
			throw new IllegalArgumentException("The list of levels to log cannot be empty");
		} else {
			mLogLevelList = levelsTolog;
		}
	}
	
	public List<LogLevel> getLevelsForStackTracePrint() {
		return mStackTraceList;
	}
	
	public void setStackTraceList(List<LogLevel> levelList) throws IllegalArgumentException {
		if(levelList == null) {
			levelList = new ArrayList<LogLevel>();
		} else if(levelList.size() > 0) {
			for(LogLevel level : levelList) {
				if(!mLogLevelList.contains(level)) {
					throw new IllegalArgumentException("You can only print the " +
							"stack trace of levels you want to log");
				}
			}
		}
		
		mStackTraceList = levelList;
	}
}
