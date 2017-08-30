package com.johnnyc.dblog;

import java.io.File;

public abstract class OnAfterCrashFile implements OnAfterCrash {

	@Override
	public void doAfterCrash(StringBuilder crashLogData) {
		doAfterCrash(new File(crashLogData.toString()));
	}

	public abstract void doAfterCrash(File crashLogFile);
	
}
