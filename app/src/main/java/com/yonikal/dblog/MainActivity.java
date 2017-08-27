package com.yonikal.dblog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.johnnyc.dblog.DBLog;
import com.johnnyc.dblog.entities.OnAfterCrash;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBLog.initialize(this, new OnAfterCrash() {
            @Override
            public void doAfterCrash(File crashLogFile) {

            }
        });
    }
}
