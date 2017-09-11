package com.johnnyc.dblog;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;

import com.johnnyc.dblog.config.LogConfiguration;
import com.johnnyc.dblog.config.LogLevel;
import com.johnnyc.dblog.utils.Utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class DBLog {

    // Status enum
    public enum Status {
        PASS, QUEUED, FAILED, EMPTY
    }

    // Private variables
    private static final String TAG = DBLog.class.getSimpleName();

    // Class Variables
    private static Queue<ContentValues> mRows = new LinkedList<ContentValues>();
    private Status mStatus;
    private static long mInstantiationTime;
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private AtomicInteger mGatherDataLocker = new AtomicInteger();
    private static SQLiteOpenHelper mDatabaseHelper;
    private static DBLog mInstance = null;
    private SQLiteDatabase mDatabase;
    //private static OnAfterCrash mOnAfterCrash;
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

    private DBLog() {
    }

    private DBLog(final LogLevel[] triggerOnLevels,
                  final boolean resetAfterTrigger, final OnAfterCrash onAfterCrash, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGatherDataLocker.incrementAndGet();
                SQLiteDatabase db = null;
                try {
                    db = getInstance().openDatabase();
                    String[] logs = new String[triggerOnLevels.length];
                    for (int i = 0; i < triggerOnLevels.length; i++) {
                        logs[i] = triggerOnLevels[i].toString();
                    }
                    String query = "SELECT * FROM " + Column.TABLE_LOGS
                            + " WHERE " + Column.LOG_LEVEL + " IN (" + makePlaceholders(logs.length) + ")";
                    Cursor cursor = db.rawQuery(query, logs);
                    if (cursor != null &&
                            cursor.moveToFirst() &&
                            cursor.getCount() > 0) {
                        final StringBuilder data = new StringBuilder();
                        try {
                            do {
                                DatePickerDialog f;
                                String logLevel;
                                String epochTime;
                                String formattedDate;
                                String pkg;
                                String className;
                                String tag;
                                String message;
                                String exceptionData;
                                com.johnnyc.dblog.entities.Log.Builder logBuilder = new com.johnnyc.dblog.entities.Log.Builder();
                                int idx = cursor.getColumnIndex(Column.LOG_LEVEL);
                                if (idx > -1) {
                                    logLevel = cursor.getString(idx);
                                    logBuilder.withLogLevel(logLevel);
                                }
                                idx = cursor.getColumnIndex(Column.LOG_EPOCH_TIME);
                                if (idx > -1) {
                                    epochTime = cursor.getString(idx);
                                    logBuilder.withEpochTime(epochTime);
                                }
                                idx = cursor.getColumnIndex(Column.LOG_FORMATTED_DATE);
                                if (idx > -1) {
                                    formattedDate = cursor.getString(idx);
                                    logBuilder.withFormattedDate(formattedDate);
                                }
                                idx = cursor.getColumnIndex(Column.PACKAGE);
                                if (idx > -1) {
                                    pkg = cursor.getString(idx);
                                    logBuilder.withPackage(pkg);
                                }
                                idx = cursor.getColumnIndex(Column.CLASS_NAME);
                                if (idx > -1) {
                                    className = cursor.getString(idx);
                                    logBuilder.withClassName(className);
                                }
                                idx = cursor.getColumnIndex(Column.TAG);
                                if (idx > -1) {
                                    tag = cursor.getString(idx);
                                    logBuilder.withTag(tag);
                                }
                                idx = cursor.getColumnIndex(Column.MESSAGE);
                                if (idx > -1) {
                                    message = cursor.getString(idx);
                                    logBuilder.withMessage(message);
                                }
                                idx = cursor.getColumnIndex(Column.EXCEPTION_DATA);
                                if (idx > -1) {
                                    exceptionData = cursor.getString(idx);
                                    logBuilder.withExceptionData(exceptionData);
                                }
                                data.append(String.format("%s%n", logBuilder.build()));
                            } while (cursor.moveToNext());
                        } finally {
                            cursor.close();
                            flush();
                        }
                        if(handler != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onAfterCrash.doAfterCrash(data);
                                }
                            });
                        }
                        if (resetAfterTrigger) {
                            reset(System.currentTimeMillis());
                        }
                    }
                } catch (SQLException ex) {
                    Log.e(TAG, ex.getMessage());
                } finally {
                    mGatherDataLocker.decrementAndGet();
                }
            }
        }).start();
    }

    public synchronized static void initialize(Context context,
                                               LogLevel[] triggerOnLevels,
                                               boolean resetAfterTrigger,
                                               OnAfterCrash onAfterCrash,
                                               Handler handler) {
        //if (mInstance == null) {
            mInstantiationTime = System.currentTimeMillis();
            mDatabaseHelper = new DBLogDatabase(context);
            mInstance = new DBLog(triggerOnLevels, resetAfterTrigger, onAfterCrash, handler);
        //}
    }

    public synchronized static void initialize(Context context) {
        if (mInstance == null) {
            mInstantiationTime = System.currentTimeMillis();
            mDatabaseHelper = new DBLogDatabase(context);
            mInstance = new DBLog();
        }
    }

    /**
     * A method used to get a new instance of the DBLog class
     *
     * @return A reference for the instance of this class
     * @throws NullPointerException In case the class has never been initialized Use
     *                              DBLog.initialize(context) to create a new instance
     *                              prior to this method
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
            String selectQuery = "SELECT * FROM " + Column.TABLE_LOGS + " WHERE " + Column.LOG_EPOCH_TIME + "<?";
            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(before)});
            //cursor = db.query(Column.TABLE_LOGS, null, null, null, null, null, null);
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
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, null, tag, msg, null);
        }
    }

    public static <T> void i(Class<?> clazz, String msg) throws NullPointerException {
        logToLogcat(LogLevel.INFO, clazz.getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, clazz, null, msg, null);
        }
    }

    public static <T> void i(Object obj, String msg) throws NullPointerException {
        logToLogcat(LogLevel.INFO, obj.getClass().getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, obj.getClass(), null, msg, null);
        }
    }

    public static <T> void i(String tag, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.INFO, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, null, tag, String.format(msg, args), null);
        }
    }

    public static <T> void i(String tag, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.INFO, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, null, tag, String.format(msg, args), exception);
        }
    }

    public static <T> void i(Class<?> clazz, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.INFO, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, clazz, null, String.format(msg, args), null);
        }
    }

    public static <T> void i(Class<?> clazz, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.INFO, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, clazz, null, String.format(msg, args), exception);
        }
    }

    public static <T> void i(Object obj, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.INFO, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, obj.getClass(), null, String.format(msg, args), null);
        }
    }

    public static <T> void i(Object obj, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.INFO, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.INFO)) {
            getInstance().addLog(LogLevel.INFO, obj.getClass(), null, String.format(msg, args), exception);
        }
    }

    // Verbose

    public static <T> void v(String tag, String msg) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, null, tag, msg, null);
        }
    }

    public static <T> void v(Class<?> clazz, String msg) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, clazz.getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, clazz, null, msg, null);
        }
    }

    public static <T> void v(Object obj, String msg) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, obj.getClass().getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, obj.getClass(), null, msg, null);
        }
    }

    public static <T> void v(String tag, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, null, tag, String.format(msg, args), null);
        }
    }

    public static <T> void v(String tag, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, null, tag, String.format(msg, args), exception);
        }
    }

    public static <T> void v(Class<?> clazz, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, clazz, null, String.format(msg, args), null);
        }
    }

    public static <T> void v(Class<?> clazz, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, clazz, null, String.format(msg, args), exception);
        }
    }

    public static <T> void v(Object obj, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, obj.getClass(), null, String.format(msg, args), null);
        }
    }

    public static <T> void v(Object obj, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.VERBOSE, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.VERBOSE)) {
            getInstance().addLog(LogLevel.VERBOSE, obj.getClass(), null, String.format(msg, args), exception);
        }
    }

    // Debug

    public static <T> void d(String tag, String msg) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, null, tag, msg, null);
        }
    }

    public static <T> void d(Class<?> clazz, String msg) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, clazz.getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, clazz, null, msg, null);
        }
    }

    public static <T> void d(Object obj, String msg) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, obj.getClass().getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, obj.getClass(), null, msg, null);
        }
    }

    public static <T> void d(String tag, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, null, tag, String.format(msg, args), null);
        }
    }

    public static <T> void d(String tag, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, null, tag, String.format(msg, args), exception);
        }
    }

    public static <T> void d(Class<?> clazz, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, clazz, null, String.format(msg, args), null);
        }
    }

    public static <T> void d(Class<?> clazz, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, clazz, null, String.format(msg, args), exception);
        }
    }

    public static <T> void d(Object obj, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, obj.getClass(), null, String.format(msg, args), null);
        }
    }

    public static <T> void d(Object obj, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.DEBUG, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.DEBUG)) {
            getInstance().addLog(LogLevel.DEBUG, obj.getClass(), null, String.format(msg, args), exception);
        }
    }

    // Warning

    public static <T> void w(String tag, String msg) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, null, tag, msg, null);
        }
    }

    public static <T> void w(Class<?> clazz, String msg) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, clazz.getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, clazz, null, msg, null);
        }
    }

    public static <T> void w(Object obj, String msg) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, obj.getClass().getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, obj.getClass(), null, msg, null);
        }
    }

    public static <T> void w(String tag, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, null, tag, String.format(msg, args), null);
        }
    }

    public static <T> void w(String tag, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, null, tag, String.format(msg, args), exception);
        }
    }

    public static <T> void w(Class<?> clazz, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, clazz, null, String.format(msg, args), null);
        }
    }

    public static <T> void w(Class<?> clazz, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, clazz, null, String.format(msg, args), exception);
        }
    }

    public static <T> void w(Object obj, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, obj.getClass(), null, String.format(msg, args), null);
        }
    }

    public static <T> void w(Object obj, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.WARNING, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.WARNING)) {
            getInstance().addLog(LogLevel.WARNING, obj.getClass(), null, String.format(msg, args), exception);
        }
    }

    // Error

    public static <T> void e(String tag, String msg) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, null, tag, msg, null);
        }
    }

    public static <T> void e(Class<?> clazz, String msg) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, clazz.getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, clazz, null, msg, null);
        }
    }

    public static <T> void e(Object obj, String msg) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, obj.getClass().getSimpleName(), msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, obj.getClass(), null, msg, null);
        }
    }

    public static <T> void e(String tag, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, null, tag, String.format(msg, args), null);
        }
    }

    public static <T> void e(String tag, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, tag, msg);
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, null, tag, String.format(msg, args), exception);
        }
    }

    public static <T> void e(Class<?> clazz, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, clazz, null, String.format(msg, args), null);
        }
    }

    public static <T> void e(Class<?> clazz, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, clazz.getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, clazz, null, String.format(msg, args), exception);
        }
    }

    public static <T> void e(Object obj, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, obj.getClass(), null, String.format(msg, args), null);
        }
    }

    public static <T> void e(Object obj, Exception exception, String msg, T... args) throws NullPointerException {
        logToLogcat(LogLevel.ERROR, obj.getClass().getSimpleName(), String.format(msg, args));
        if (Configuration.getLevelsToLog().contains(LogLevel.ERROR)) {
            getInstance().addLog(LogLevel.ERROR, obj.getClass(), null, String.format(msg, args), exception);
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
            case WTF:
                Log.wtf(tag, msg);
            default:
                Log.e(tag, msg);
                break;
        }
    }

    private Status flush() {
        Status status = Status.EMPTY;
        long rowId = -1;
        SQLiteDatabase db = null;
        for (int i = 0; i < mRows.size(); i++) {
            ContentValues row = mRows.poll();
            if (row != null) {
                try {
                    db = getInstance().openDatabase();
                    rowId = db.insert(Column.TABLE_LOGS, null, row);

                    // If it fails once fail the entire process
                    if (rowId > -1 && !status.equals(Status.FAILED)) {
                        status = Status.PASS;
                    } else {
                        status = Status.FAILED;
                    }
                } catch (Exception e) {
                    return Status.FAILED;
                }
            }
        }

        return status;
    }

    private Status addLog(LogLevel level, Class<?> clazz, String tag, String messege, Exception exception) {
        // method variables
        long rowId = -1;
        Status status = Status.EMPTY;
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
            if (clazz != null) {
                row.put(Column.PACKAGE, clazz.getPackage().getName());
                row.put(Column.CLASS_NAME, clazz.getSimpleName());
            }
            if (tag != null) {
                row.put(Column.TAG, tag);
            }
            if (messege != null) {
                row.put(Column.MESSAGE, messege);
            }
            if (exception != null) {
                row.put(Column.EXCEPTION_DATA, Utils.getStackTraceAsString(exception));
            }

            if (mGatherDataLocker.get() == 1) {
                status = Status.QUEUED;
                mRows.add(row);
            } else {
                rowId = db.insert(Column.TABLE_LOGS, null, row);
            }
            if (rowId > -1) {
                status = Status.PASS;
            }
        } catch (SQLException ex) {
            status = Status.FAILED;
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
        return status;
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }
}
