package com.wbrawner.simplemarkdown;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String AUTHORITY = "com.wbrawner.simplemarkdown.fileprovider";
    private static final int REQUEST_WRITE_STORAGE = 0;
    private static File mFilesDir;
    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.layout_tab) TabLayout tabLayout;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static String fileName;

    public static String getTempFileName() {
        return "tmp_" + getFileName();
    }

    public static String getFileName() {
        if (fileName == null) {
            return "untitled.md";
        }
        if (!fileName.endsWith(".md"))
            return fileName + ".md";
        return fileName;
    }

    public static String getTempFilePath() {
        return mFilesDir + "/tmp/";
    }

    public static String getFilePath() {
        return mFilesDir + "/saved_files/";
    }

    public static void setFileName(String fileName) {
        MainActivity.fileName = fileName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        pager.setAdapter(new EditPagerAdapter(getSupportFragmentManager()));
        mFilesDir = getFilesDir();
        checkDirectories();
        Intent intent = getIntent();
        if (intent != null && !intent.getAction().equals(Intent.ACTION_MAIN) && intent.getData() != null) {
            Intent loadIntent = new Intent(EditFragment.LOAD_ACTION);
            loadIntent.putExtra("fileUri", intent.getData().toString());
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(loadIntent);
        }
    }

    private void checkDirectories() {
        File tmpDir = new File(getTempFilePath());
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        File outDir = new File(getFilePath());
        if (!outDir.exists()) {
            outDir.mkdir();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //TODO: Create popup for file name
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.action_save);

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint(R.string.hint_filename);
                input.setText(getFileName());
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().length() > 0) {
                            setFileName(input.getText().toString());
                            Intent saveIntent = new Intent(EditFragment.SAVE_ACTION);
                            saveIntent.putExtra("fileName", input.getText());
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(saveIntent);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            case R.id.action_share:
                //TODO: Fix this
                File tmpFile = new File(getTempFilePath() + getFileName());
                if (!tmpFile.exists()) {
                    Intent saveIntent = new Intent(EditFragment.SAVE_ACTION);
                    saveIntent.putExtra("fileName", getTempFilePath() + getFileName());
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(saveIntent);
                }
                Log.d(TAG, "Temp file size: " + tmpFile.length());
                Uri fileUri = FileProvider.getUriForFile(MainActivity.this, AUTHORITY, tmpFile);
//                Uri fileUri = Uri.fromFile(tmpFile);
                if (fileUri != null) {
                    Log.d(TAG, "MIME type: " + getContentResolver().getType(fileUri));
                    try {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setDataAndType(
                                fileUri,
                                getContentResolver().getType(fileUri)
                        );
                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(
                                Intent.createChooser(
                                        shareIntent,
                                        getString(R.string.share_file)
                                )
                        );
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Error sharing file", e);
                        Toast.makeText(
                                MainActivity.this,
                                R.string.no_shareable_apps,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
                break;
//            case R.id.action_export:
//                //TODO: Implement export functionality
//                if (ContextCompat.checkSelfPermission(MainActivity.this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED){
//                ActivityCompat.requestPermissions(MainActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        REQUEST_WRITE_STORAGE);
//                break;
//            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //TODO: Implement export functionality
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, open file chooser dialog

                } else {
                    // Permission denied, do nothing
                }
                return;
            }
        }
    }

    public class EditPagerAdapter extends FragmentPagerAdapter {
        private static final int FRAGMENT_EDIT = 0;
        private static final int FRAGMENT_PREVIEW = 1;
        private static final int NUM_PAGES = 2;

        public EditPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case FRAGMENT_EDIT:
                    return new EditFragment();
                case FRAGMENT_PREVIEW:
                    return new PreviewFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int stringId = 0;
            switch (position) {
                case FRAGMENT_EDIT:
                    stringId = R.string.action_edit;
                    break;
                case FRAGMENT_PREVIEW:
                    stringId = R.string.action_preview;
                    break;
            }
            return getString(stringId);
        }
    }
}
