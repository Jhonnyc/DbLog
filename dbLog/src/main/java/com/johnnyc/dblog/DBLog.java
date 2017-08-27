package com.johnnyc.dblog;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.johnnyc.dblog.config.LogConfiguration;
import com.johnnyc.dblog.config.LogLevel;
import com.johnnyc.dblog.entities.OnAfterCrash;
import com.johnnyc.dblog.utils.Utils;

public class DBLog {

	// Private variables
	private static final String TAG = DBLog.class.getSimpleName();

	// Class Variables
	private static long mInstantiationTime;
	private AtomicInteger mOpenCounter = new AtomicInteger();
	private static SQLiteOpenHelper mDatabaseHelper;
	private static DBLog mInstance = null;
	private SQLiteDatabase mDatabase;
	private static OnAfterCrash mOnAfterCrash;
	public static LogConfiguration Configuration = new LogConfiguration();

	public static abstract class Column implements BaseColumns {
		public static final String TABLE_LOGS = "logs_table";
		public static final String LOG_LEVEL = "log_level";
		public static final String LOG_EPOCH_TIME = "log_epoch_time";
		public static final String LOG_FORMATTED_DATE = "log_formatted_time";
		public static final String PACKAGE = "package";
		public static final String CLASS_NAME = "class_name";
		public static final String TAG = "tag";
		public static final String MESSAGE = "message";
		public static final String EXCEPTION_DATA = "exception_data";
	}

	public synchronized static void initialize(Context context, OnAfterCrash onAfterCrash) {
		if (mInstance == null) {
			mInstantiationTime = System.currentTimeMillis();
			mInstance = new DBLog();
			mDatabaseHelper = new DBLogDatabase(context);
		}
		if(onAfterCrash == null) {
			throw new IllegalArgumentException("OnAfterCrash cannot be null!");
		} else {
			mOnAfterCrash = onAfterCrash;
		}
	}

	/**
	 * A method used to get a new instance of the DBLog class
	 * 
	 * @return A reference for the instance of this class
	 * @throws NullPointerException
	 *             In case the class has never been initialized Use
	 *             DBLog.initialize(context) to create a new instance
	 *             prior to this method
	 */
	public static DBLog getInstance() throws NullPointerException {
		if (mInstance == null) {
			throw new NullPointerException(
					"The class has never been initialized. "
							+ "Use initialize(context) first to create a new instance");
		}
		return mInstance;
	}

	public boolean reset(long before) {
		int rowsDeleted;
		int totalRows;
		Cursor cursor;
		boolean pass = false;
		SQLiteDatabase db = null;
		try {
			db = openDatabase();
			cursor = db.query(Column.TABLE_LOGS, null, null, null, null, null, null);
			totalRows = cursor.getCount();
			rowsDeleted = db.delete(Column.TABLE_LOGS, null, null);
			pass = rowsDeleted == totalRows;
		} catch (SQLException exception) {
			pass = false;
			Log.e(TAG, exception.getMessage());
		} finally {
			if (db != null) {
				// close database connection
				closeDatabase();
			}
		}
		return pass;
	}
	
	/**********************************
	***********************************
	********* Logging Methods *********
	***********************************
	***********************************/

	// Info

	public static <T> void i(String tag, String msg) throws NullPointerException {
		logToLogcat(LogLevel.INFO, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, null, tag, msg, null);
		}
	}

	public static <T> void i(Class<?> clazz, String msg) throws NullPointerException {
		logToLogcat(LogLevel.INFO, clazz.getSimpleName(), msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, clazz, null, msg, null);
		}
	}

	public static <T> void i(Object obj, String msg) throws NullPointerException {
		logToLogcat(LogLevel.INFO, obj.getClass().getSimpleName(), msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, obj.getClass(), null, msg, null);
		}
	}

	public static <T> void i(String tag, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.INFO, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, null, tag, String.format(msg, args), null);
		}
	}

	public static <T> void i(String tag, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.INFO, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, null, tag, String.format(msg, args), exception);
		}
	}

	public static <T> void i(Class<?> clazz, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.INFO, clazz.getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, clazz, null, String.format(msg, args), null);
		}
	}

	public static <T> void i(Class<?> clazz, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.INFO, clazz.getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, clazz, null, String.format(msg, args), exception);
		}
	}

	public static <T> void i(Object obj, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.INFO, obj.getClass().getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, obj.getClass(), null, String.format(msg, args), null);
		}
	}

	public static <T> void i(Object obj, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.INFO, obj.getClass().getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
			getInstance().addLog(LogLevel.INFO, obj.getClass(), null, String.format(msg, args), exception);
		}
	}

	// Verbose 
	
	public static <T> void v(String tag, String msg) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, null, tag, msg, null);
		}
	}
	
	public static <T> void v(Class<?> clazz, String msg) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, clazz.getSimpleName(), msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, clazz, null, msg, null);
		}
	}
	
	public static <T> void v(Object obj, String msg) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, obj.getClass().getSimpleName(), msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, obj.getClass(), null, msg, null);
		}
	}
	
	public static <T> void v(String tag, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, null, tag, String.format(msg, args), null);
		}
	}
	
	public static <T> void v(String tag, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, null, tag, String.format(msg, args), exception);
		}
	}
	
	public static <T> void v(Class<?> clazz, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, clazz.getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, clazz, null, String.format(msg, args), null);
		}
	}
	
	public static <T> void v(Class<?> clazz, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, clazz.getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, clazz, null, String.format(msg, args), exception);
		}
	}
	
	public static <T> void v(Object obj, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, obj.getClass().getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, obj.getClass(), null, String.format(msg, args), null);
		}
	}
	
	public static <T> void v(Object obj, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.VERBOSE, obj.getClass().getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
			getInstance().addLog(LogLevel.VERBOSE, obj.getClass(), null, String.format(msg, args), exception);
		}
	}

	// Debug

	public static <T> void d(String tag, String msg) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, null, tag, msg, null);
		}
	}

	public static <T> void d(Class<?> clazz, String msg) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, clazz.getSimpleName(), msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, clazz, null, msg, null);
		}
	}

	public static <T> void d(Object obj, String msg) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, obj.getClass().getSimpleName(), msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, obj.getClass(), null, msg, null);
		}
	}

	public static <T> void d(String tag, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, null, tag, String.format(msg, args), null);
		}
	}

	public static <T> void d(String tag, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, tag, msg);
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, null, tag, String.format(msg, args), exception);
		}
	}

	public static <T> void d(Class<?> clazz, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, clazz.getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, clazz, null, String.format(msg, args), null);
		}
	}

	public static <T> void d(Class<?> clazz, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, clazz.getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, clazz, null, String.format(msg, args), exception);
		}
	}

	public static <T> void d(Object obj, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, obj.getClass().getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, obj.getClass(), null, String.format(msg, args), null);
		}
	}

	public static <T> void d(Object obj, Exception exception, String msg, T... args) throws NullPointerException {
		logToLogcat(LogLevel.DEBUG, obj.getClass().getSimpleName(), String.format(msg, args));
		if(Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
			getInstance().addLog(LogLevel.DEBUG, obj.getClass(), null, String.format(msg, args), exception);
		}
	}

	/*******************************
	 ******************************* 
	 ******* Private Methods *******
	 *******************************
	 *******************************/
	
	private synchronized SQLiteDatabase openDatabase() {
		if (mOpenCounter.incrementAndGet() == 1) {
			// Opening new database
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		return mDatabase;
	}

	private synchronized void closeDatabase() {
		if (mOpenCounter.decrementAndGet() == 0) {
			// Closing database
			mDatabase.close();

		}
	}
	
	private static void logToLogcat(LogLevel level, String tag, String msg) {
		switch (level) {
		case DEBUG:
			Log.d(tag, msg);
			break;
		case ERROR:
			Log.e(tag, msg);
			break;
		case INFO:
			Log.i(tag, msg);
			break;
		case VERBOSE:
			Log.v(tag, msg);
			break;
		case WARNING:
			Log.w(tag, msg);
			break;
		default:
			Log.e(tag, msg);
			break;
		}
	}

	private boolean addLog(LogLevel level, Class<?> clazz, String tag, String messege, Exception exception) {
		// method variables
		long rowId;
		boolean pass = false;
		SQLiteDatabase db = null;
		ContentValues row = null;
		long epochTime = System.currentTimeMillis();
		String formmatedTime = Utils.getStringDateFromEpochTime(epochTime, Configuration.getTimeFormat());

		try {
			db = getInstance().openDatabase();
			row = new ContentValues();
			row.put(Column.LOG_LEVEL, level.toString());
			row.put(Column.LOG_EPOCH_TIME, epochTime);
			row.put(Column.LOG_FORMATTED_DATE, formmatedTime);
			if(clazz != null) {
				row.put(Column.PACKAGE, clazz.getPackage().getName());
				row.put(Column.CLASS_NAME, clazz.getSimpleName());
			}
			if(tag != null) {
				row.put(Column.TAG, tag);
			}
			if(messege != null) {
				row.put(Column.MESSAGE, messege);
			}
			if(exception != null) {
				row.put(Column.EXCEPTION_DATA, Utils.getStackTraceAsString(exception));
			}
			
			rowId = db.insert(Column.TABLE_LOGS, null, row);
			if (rowId > -1) {
				pass = true;
			}
		} catch (SQLException ex) {
			pass = false;
			Log.e(TAG, ex.getMessage());
		} finally {
			try {
				if (db != null) {
					// close database connection
					getInstance().closeDatabase();
				}
			} catch (SQLException ex) {
				Log.e(TAG, ex.getMessage());
			}
		}
		return pass;
	}
}
