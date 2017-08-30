package com.johnnyc.dblog;

public abstract class OnAfterCrashStringBuilder implements OnAfterCrash {

    public abstract void doAfterCrash(StringBuilder crashLogData);

}
