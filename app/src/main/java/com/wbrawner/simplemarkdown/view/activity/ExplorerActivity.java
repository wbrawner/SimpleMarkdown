package com.wbrawner.simplemarkdown.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.utility.Constants;
import com.wbrawner.simplemarkdown.utility.Utils;

import org.acra.ACRA;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public class ExplorerActivity extends AppCompatActivity {
    private Handler fileHandler = Utils.createSafeHandler("ExplorerThread");
    private ListView listView;
    private File[] mounts;
    private String docsDirPath;
    private AtomicReference<String> filePath = new AtomicReference<>("");
    private volatile boolean isSave = false;
    private volatile boolean showFiles = true;
    private EditText fileName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(Constants.EXTRA_REQUEST_CODE)) {
            finish();
            return;
        }

        docsDirPath = Utils.getDocsPath(this);

        int requestCode = intent.getIntExtra(Constants.EXTRA_REQUEST_CODE, -1);
        switch (requestCode) {
            case Constants.REQUEST_OPEN_FILE:
                break;
            case Constants.REQUEST_ROOT_DIR:
                showFiles = false;
                break;
            case Constants.REQUEST_SAVE_FILE:
                isSave = true;
                fileName = findViewById(R.id.file_name);
                fileName.setVisibility(View.VISIBLE);
                if (intent.hasExtra(Constants.EXTRA_FILE)) {
                    File file = (File) intent.getSerializableExtra(Constants.EXTRA_FILE);
                    if (file.exists() && file.canWrite()) {
                        docsDirPath = file.getParentFile().getAbsolutePath();
                        fileName.setText(file.getName());
                    } else {
                        fileName.setText("Untitled.md");
                    }
                }
                Button saveButton = findViewById(R.id.button_save);
                saveButton.setVisibility(View.VISIBLE);
                saveButton.setOnClickListener((v) -> {
                    Intent fileIntent = new Intent();
                    String absolutePath = String.format(
                            Locale.ENGLISH,
                            "%s/%s",
                            filePath.get(),
                            fileName.getText().toString()
                    );
                    fileIntent.putExtra(Constants.EXTRA_FILE_PATH, absolutePath);
                    setResult(RESULT_OK, fileIntent);
                    finish();
                });
                break;
            default:
                finish();
                return;
        }
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(view ->
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null)
//                        .show()
//        );

        listView = findViewById(R.id.file_list);
        updateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_explorer, menu);

        if (!showFiles) {
            menu.findItem(R.id.action_select).setVisible(true);
        }

        if (hasRemovableStorage()) {
            menu.findItem(R.id.action_use_sdcard).setVisible(true);
            boolean sdcardSelected = false;
            try {
                sdcardSelected = filePath.get().contains(mounts[1].getAbsolutePath());
            } catch (NullPointerException e) {
                ACRA.getErrorReporter().handleException(e, false);
                updateListView();
                menu.findItem(R.id.action_use_sdcard).setVisible(false);
            }
            if (sdcardSelected) {
                menu.findItem(R.id.action_use_sdcard).setChecked(true);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_use_sdcard:
                if (!hasRemovableStorage()) {
                    // We shouldn't get here to begin with but better safe than sorry
                    break;
                }
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    updateListView(mounts[1]);
                } else {
                    updateListView(new File(docsDirPath));
                }
                break;
            case R.id.action_select:
                replyWithFile(new File(filePath.get()));
                break;
        }
        return true;
    }

    private boolean hasRemovableStorage() {
        mounts = getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS);
        return mounts.length > 1;
    }

    @SuppressWarnings("unchecked")
    private List<HashMap<String, Object>> loadFiles(File docsDir) {
        TreeSet<HashMap<String, Object>> files = new TreeSet<HashMap<String, Object>>((o1, o2) ->
                ((String) o1.get("name")).compareToIgnoreCase((String) o2.get("name"))) {
        };
        TreeSet<HashMap<String, Object>> dirs = new TreeSet<HashMap<String, Object>>((o1, o2) ->
                ((String) o1.get("name")).compareToIgnoreCase((String) o2.get("name"))) {
        };

        if (docsDir.listFiles() != null) {
            for (File file : docsDir.listFiles()) {
                if (file.isDirectory()) {
                    HashMap<String, Object> fileHashMap = new HashMap<>();
                    fileHashMap.put("name", file.getName());
                    fileHashMap.put("file", file);
                    dirs.add(fileHashMap);
                    continue;
                }
                if (!showFiles ||
                        (!file.getName().endsWith("md")
                                && !file.getName().endsWith("markdown")
                                && !file.getName().endsWith("text"))) {
                    continue;
                }
                HashMap<String, Object> fileHashMap = new HashMap<>();
                fileHashMap.put("name", file.getName());
                fileHashMap.put("file", file);
                files.add(fileHashMap);
            }
        }

        List<HashMap<String, Object>> sortedFiles = new ArrayList<>();
        if (docsDir.getParentFile() != null && docsDir.getParentFile().canRead()) {
            HashMap<String, Object> fileHashMap = new HashMap<>();
            fileHashMap.put("name", getString(R.string.directory_up));
            fileHashMap.put("file", docsDir.getParentFile());

            sortedFiles.add(fileHashMap);
        }

        sortedFiles.addAll(dirs);
        sortedFiles.addAll(files);

        return sortedFiles;
    }

    private void updateListView() {
        File docsDir = new File(docsDirPath);
        if (!docsDir.exists()) {
            docsDir = Environment.getExternalStorageDirectory();
        }
        updateListView(docsDir);
    }

    private void updateListView(File filesDir) {
        setTitle(filesDir.getName());
        filePath.set(filesDir.getAbsolutePath());
        fileHandler.post(() -> {
            final List<HashMap<String, Object>> files = loadFiles(filesDir);

            runOnUiThread(() -> {
                listView.setAdapter(new SimpleAdapter(
                        this,
                        files,
                        android.R.layout.simple_list_item_1,
                        new String[]{"name"},
                        new int[]{android.R.id.text1}
                ));

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    File clickedFile = (File) files.get(position).get("file");
                    if (clickedFile.isFile()) {
                        handleFileClick(clickedFile);
                    } else if (clickedFile.isDirectory()) {
                        updateListView(clickedFile);
                    }
                });
            });
        });
    }

    void handleFileClick(File file) {
        if (isSave) {
            if (fileName != null) {
                fileName.setText(file.getName());
            }
        } else {
            replyWithFile(file);
        }
    }

    private void replyWithFile(File file) {
        Intent fileIntent = new Intent();
        fileIntent.putExtra(Constants.EXTRA_FILE, file);
        setResult(RESULT_OK, fileIntent);
        finish();
    }
}
