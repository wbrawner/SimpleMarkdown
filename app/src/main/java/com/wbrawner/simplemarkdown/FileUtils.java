package com.wbrawner.simplemarkdown;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by billy on 7/27/2017.
 */

public class FileUtils {

    public static final int WRITE_PERMISSION_REQUEST = 0;
    public static final int OPEN_FILE_REQUEST = 1;

    private Activity mContext;

    public FileUtils(Activity context) {
        mContext = context;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageWriteable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) && checkWritePermission()) {
            return true;
        }
        return false;
    }

    public boolean checkWritePermission() {
        return (ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED);
    }

    public void requestWritePermissions() {
        ActivityCompat.requestPermissions(
                mContext,
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_PERMISSION_REQUEST
        );
    }
}
