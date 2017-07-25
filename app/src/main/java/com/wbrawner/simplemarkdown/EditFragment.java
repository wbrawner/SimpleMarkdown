package com.wbrawner.simplemarkdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;


public class EditFragment extends Fragment {
    public static final String SAVE_ACTION = "com.wbrawner.simplemarkdown.ACTION_SAVE";
    @BindView(R.id.markdown_edit) EditText markdownEditor;

    private File mTmpFile;

    public EditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SAVE_ACTION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                new EditFragment.MarkdownBroadcastSaveReceiver(),
                filter
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        ButterKnife.bind(this, view);
        if (markdownEditor.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        };
        markdownEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updatePreview(markdownEditor.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "Markdown from beginning: " + markdownEditor.getText().toString());
        BufferedReader reader = null;
        try {
            File tmpFile = new File(getActivity().getFilesDir() + MainActivity.getTempFileName());
            if (tmpFile.exists()) {
                Log.d(TAG, "Temp file size: " + tmpFile.length());
                InputStream in = new FileInputStream(tmpFile);
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    markdownEditor.append(line);
                    markdownEditor.append("\r\n");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading temp file: ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        if (markdownEditor.getText().length() > 0) {
            Log.d(TAG, "We have text");
        } else {
            Log.d(TAG, "Blank slate");
        }
        updatePreview(markdownEditor.getText());
    }

    private void updatePreview(Editable data) {
        Log.d(TAG, "Data to send: " + data.toString());
        Intent broadcastIntent = new Intent(PreviewFragment.PREVIEW_ACTION);
        broadcastIntent.putExtra("markdownData", data.toString());
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.sendBroadcast(broadcastIntent);
    }

    public void save(String data, String filePath) {
        if (filePath == null)
            filePath = MainActivity.getFilePath() + MainActivity.getFileName();
        FileOutputStream out = null;
        try {
            File tmpFile = new File(filePath);
            out = new FileOutputStream(tmpFile);
            out.write(data.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "Error saving temp file:", e);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing write stream", e);
            }
        }
    }

    public void save(String data) {
        save(data, null);
    }

    public void save(Editable data) {
        save(data.toString(), null);
    }

    @Override
    public void onPause() {
        save(markdownEditor.getText().toString(),
                MainActivity.getTempFilePath() + MainActivity.getFileName());
        super.onPause();
    }


    private class MarkdownBroadcastSaveReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("fileName")) {
                String fileName = intent.getStringExtra("fileName");
                Log.d(TAG, "File: " + fileName);
                save(markdownEditor.getText().toString(), fileName);
            }
        }
    }
}
