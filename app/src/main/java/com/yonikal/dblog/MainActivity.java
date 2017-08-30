package com.yonikal.dblog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.johnnyc.dblog.DBLog;
import com.johnnyc.dblog.OnAfterCrashStringBuilder;
import com.johnnyc.dblog.config.LogLevel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogLevel[] logs = { LogLevel.ERROR, LogLevel.DEBUG };
        DBLog.initialize(this, logs, true, new OnAfterCrashStringBuilder() {

            @Override
            public void doAfterCrash(StringBuilder crashLogData) {
                String g = "sasd";
                TextView tv = (TextView) findViewById(R.id.logs);
                tv.setText(crashLogData.toString());
            }
        });
        DBLog.d(this, "1");
        DBLog.d(this, "2");
        DBLog.d(this, "3");
    }
}
