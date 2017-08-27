package com.johnnyc.dblog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.johnnyc.dblog.DBLog.Column;

public class DBLogDatabase extends SQLiteOpenHelper {

	// Database name version and table name
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_LOGS_DATA = "logs_database";

	// Tables and table columns names
	private String CREATE_LOGS_TABLE;

	/*
	 * A package private class constructor
	 */
	protected DBLogDatabase(Context context) {
		super(context, DATABASE_LOGS_DATA, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		CREATE_LOGS_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ Column.TABLE_LOGS + " ("
				+ Column.LOG_LEVEL + " TEXT, "
				+ Column.LOG_EPOCH_TIME + " TEXT, "
				+ Column.LOG_FORMATTED_DATE + " TEXT, "
				+ Column.PACKAGE + " TEXT, "
				+ Column.CLASS_NAME + " TEXT, "
				+ Column.TAG + " TEXT, "
				+ Column.MESSAGE + " TEXT, "
				+ Column.EXCEPTION_DATA + " TEXT);";

		// create the tables
		db.execSQL(CREATE_LOGS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + Column.TABLE_LOGS);
		onCreate(db);
	}
}
