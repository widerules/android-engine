package org.chemodansama.engine;

import android.util.Log;

public abstract class LogHelper {
    public static void e(String msg) {
        if (Log.isLoggable(LogTag.TAG, Log.ERROR)) {
            Log.e(LogTag.TAG, msg);
        }
    }
    
    public static void w(String msg) {
        if (Log.isLoggable(LogTag.TAG, Log.WARN)) {
            Log.w(LogTag.TAG, msg);
        }
    }
    
    public static void i(String msg) {
        if (Log.isLoggable(LogTag.TAG, Log.INFO)) {
            Log.i(LogTag.TAG, msg);
        }
    }
}
