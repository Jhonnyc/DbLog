package com.yonikal.dblog;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.johnnyc.dblog.DBLog;
import com.johnnyc.dblog.OnAfterCrashStringBuilder;
import com.johnnyc.dblog.config.LogLevel;

public class MainActivity extends AppCompatActivity {

    private static int i = 1;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.logs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogLevel[] logs = { LogLevel.ERROR, LogLevel.DEBUG };
        DBLog.initialize(this, logs, true, new OnAfterCrashStringBuilder() {

            @Override
            public void doAfterCrash(StringBuilder crashLogData) {
                if(crashLogData.length() < 1) {
                    tv.setText("Empty");
                } else {
                    tv.setText(crashLogData.toString());
                }
            }
        }, new Handler());
        /*DBLog.initialize(this, logs, true, new OnAfterCrashStringBuilder() {

            @Override
            public void doAfterCrash(StringBuilder crashLogData) {
                String g = "sasd";
                TextView tv = (TextView) findViewById(R.id.logs);
                tv.setText(crashLogData.toString());
            }
        });*/

        findViewById(R.id.btn_add_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBLog.d(this, String.valueOf(i));
                i++;
                DBLog.d(this, String.valueOf(i));
                i++;
                DBLog.d(this, String.valueOf(i));
                i++;
            }
        });
    }
}
