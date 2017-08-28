package com.johnnyc.dblog.entities;

/**
 * Created by yoni on 27/08/2017.
 */

public class Log {

    private String mLogLevel;
    private String mEpochTime;
    private String mFormattedDate;
    private String mPkg;
    private String mClassName;
    private String mTag;
    private String mMessage;
    private String mExceptionData;

    private Log() {}

    public static class Builder {
        private String logLevel;
        private String epochTime;
        private String formattedDate;
        private String pkg;
        private String className;
        private String tag;
        private String message;
        private String exceptionData;

        public void withLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public void withEpochTime(String epochTime) {
            this.epochTime = epochTime;
        }

        public void withFormattedDate(String formattedDate) {
            this.formattedDate = formattedDate;
        }

        public void withPackage(String pkg) {
            this.pkg = pkg;
        }

        public void withClassName(String className) {
            this.className = className;
        }

        public void withTag(String tag) {
            this.tag = tag;
        }

        public void withMessage(String message) {
            this.message = message;
        }

        public void withExceptionData(String exceptionData) {
            this.exceptionData = exceptionData;
        }

        public String build() {
            String description = "";
            if(tag != null) {
                description += tag + " ";
            }
            if(pkg != null) {
                description += pkg + " ";
            }
            if(className != null) {
                description += className + " ";
            }
            description += "|";
            if(logLevel != null) {
                description += " " + logLevel + " ";
            }
            description += "|";
            if(formattedDate != null) {
                description += " " + formattedDate + " ";
            }
            if(epochTime != null) {
                description += epochTime + " ";
            }
            description += "|";
            if(message != null) {
                description += " " + message + " ";
                description += "|";
            }
            if(exceptionData != null) {
                description += " " + exceptionData;
            }

            if(description.charAt(description.length() - 1) == '|') {
                description = description.substring(0, description.length() - 1);
            }

            return description.trim();
        }
    }
}
