package com.wbrawner.simplemarkdown;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;

/**
 * Created by billy on 7/25/17.
 */

public class FileLoadTask extends AsyncTask<Uri, Void, String> {

    private Context mContext;
    private EditText mMarkdownEditor;

    public FileLoadTask(Context context, EditText markdownEditor) {
        mContext = context;
        mMarkdownEditor = markdownEditor;
    }

    @Override
    protected String doInBackground(Uri... uris) {
        if (mContext == null) {
            Log.d(TAG, "No context, abort");
            return null;
        }
        Log.d(TAG, "Begin loading file");
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            InputStream in = mContext.getContentResolver().openInputStream(uris[0]);
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
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
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        mMarkdownEditor.setText(s);
        super.onPostExecute(s);
    }
}
