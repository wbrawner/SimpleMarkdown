package com.wbrawner.simplemarkdown.utility;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

import java.io.Closeable;
import java.io.IOException;

public class Utils {

    public static final String KEY_AUTOSAVE = "autosave";

    public static boolean isAutosaveEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(
                KEY_AUTOSAVE,
                true
        );
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean canAccessFiles(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }


    @SuppressWarnings("SameParameterValue")
    public static Handler createSafeHandler(ErrorHandler errorHandler, String name) {
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        handlerThread.setUncaughtExceptionHandler((t, e) -> {
            errorHandler.reportException(e);
            t.interrupt();
        });
        return new Handler(handlerThread.getLooper());
    }
}
