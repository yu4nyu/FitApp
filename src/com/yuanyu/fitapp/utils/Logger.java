
package com.yuanyu.fitapp.utils;

import android.util.Log;

import java.util.Locale;

public abstract class Logger {
    private static final String TAG_PREFIX = "AHCntr";
    private static final boolean LOG_ENABLED = true;

    private static final LoggerFactory FACTORY =
            LOG_ENABLED ? new LogcatLoggerFactory() : new NoLogLoggerFactory();

    public static Logger getInstance(String tag) {
        return FACTORY.getLogger(tag);
    }

    /** uses the caller's classname */
    public static Logger getInstance() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 0 : VMStack
        // 1 : Thread
        // 2 : Logger
        // 3 : calling class
        String className = stackTrace[3].getClassName();
        int lastDot = className.lastIndexOf('.');
        String simpleClassName = lastDot != -1 ? className.substring(lastDot) : className;
        return getInstance(simpleClassName);
    }

    private interface LoggerFactory {
        Logger getLogger(String tag);
    }

    private static class LogcatLoggerFactory implements LoggerFactory {

        public LogcatLoggerFactory() {
            // nothing
        }

        @Override
        public Logger getLogger(String tag) {
            return new LogcatLogger(tag);
        }
    }

    private static class NoLogLoggerFactory implements LoggerFactory {

        public NoLogLoggerFactory() {
            // nothing
        }

        @Override
        public Logger getLogger(String tag) {
            return new NoLogLogger();
        }
    }

    public abstract void d(String message);

    public abstract void d(String format, Object... args);

    public abstract void d(String message, Throwable tr);

    public abstract void d(String format, Throwable tr, Object... args);

    public abstract void e(String message);

    public abstract void e(String format, Object... args);

    public abstract void e(String message, Throwable tr);

    public abstract void e(String format, Throwable tr, Object... args);

    public abstract void e(Throwable tr);

    private static class NoLogLogger extends Logger {

        public NoLogLogger() {
            // empty
        }

        @Override
        public void d(String message) {
            // empty on purpose
        }

        @Override
        public void d(String message, Throwable tr) {
            // empty on purpose
        }

        @Override
        public void d(String format, Object... args) {
            // empty on purpose
        }

        @Override
        public void d(String format, Throwable tr, Object... args) {
            // empty on purpose
        }

        @Override
        public void e(String message) {
            // empty on purpose
        }

        @Override
        public void e(String format, Object... args) {
            // empty on purpose
        }

        @Override
        public void e(String message, Throwable tr) {
            // empty on purpose
        }

        @Override
        public void e(String format, Throwable tr, Object... args) {
            // empty on purpose
        }

        @Override
        public void e(Throwable tr) {
            // empty on purpose
        }

    }

    private static class LogcatLogger extends Logger {
        private final String mTag;

        public LogcatLogger(String tag) {
            mTag = TAG_PREFIX + "." + tag;
        }

        @Override
        public void d(String message) {
            Log.d(mTag, message);
        }

        @Override
        public void d(String message, Throwable tr) {
            Log.d(mTag, message, tr);
        }

        @Override
        public void d(String format, Object... args) {
            d(String.format(Locale.ROOT, format, args));
        }

        @Override
        public void d(String format, Throwable tr, Object... args) {
            d(String.format(Locale.ROOT, format, args), tr);
        }

        @Override
        public void e(String message) {
            Log.e(mTag, message);
        }

        @Override
        public void e(String message, Throwable tr) {
            Log.e(mTag, message, tr);
        }

        @Override
        public void e(String format, Object... args) {
            e(String.format(Locale.ROOT, format, args));
        }

        @Override
        public void e(String format, Throwable tr, Object... args) {
            e(String.format(Locale.ROOT, format, args), tr);
        }

        @Override
        public void e(Throwable tr) {
            e("", tr);
        }
    }
}
