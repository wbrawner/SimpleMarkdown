package com.wbrawner.simplemarkdown;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;

/**
 * Created by billy on 7/25/17.
 */

public class FileLoadTask extends AsyncTask<Uri, String, String> {

    private Context mContext;
    private EditFragment mEditFragment;

    public FileLoadTask(Context context, EditFragment editFragment) {
        mContext = context;
        mEditFragment = editFragment;
    }

    @Override
    protected String doInBackground(Uri... uris) {
        if (mContext == null) {
            return null;
        }
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        FileOutputStream out = null;
        try {
            InputStream in = mContext.getContentResolver().openInputStream(uris[0]);
            File tmpFile = new File(mContext.getFilesDir() + "/" + MainActivity.getTempFileName());
            if (tmpFile.exists())
                tmpFile.delete();
            out = new FileOutputStream(tmpFile);
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
                out.write(line.getBytes());
                out.write("\r\n".getBytes());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening file:", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {}
            }
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mEditFragment.setEditorText(s);
    }
}