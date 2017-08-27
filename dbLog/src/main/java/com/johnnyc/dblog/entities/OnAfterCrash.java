package com.johnnyc.dblog.entities;

import java.io.File;

public abstract class OnAfterCrash {

	public abstract void doAfterCrash(File crashLogFile);
	
}
