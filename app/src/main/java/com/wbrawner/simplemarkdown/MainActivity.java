package com.wbrawner.simplemarkdown;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.EditText;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String AUTHORITY = "com.wbrawner.simplemarkdown.fileprovider";
    private static File mFilesDir;
    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.layout_tab) TabLayout tabLayout;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static String fileName;

    public static String getTempFileName() {
        return "com_wbrawner_simplemarkdown_tmp.md";
    }

    public static String getFileName() {
        if (fileName == null) {
            return getTempFileName();
        }
        return fileName;
    }

    public static String getTempFilePath() {
        return mFilesDir + File.pathSeparator + "tmp" + File.pathSeparator;
    }

    public static String getFilePath() {
        return mFilesDir + File.pathSeparator + "saved_files" + File.pathSeparator;
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
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_edit, menu);
//        return true;
//    }

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
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent saveIntent = new Intent(EditFragment.SAVE_ACTION);
                        saveIntent.putExtra("fileName", input.getText());
                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(saveIntent);
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
                File tmpFile = new File(getFilesDir() + getTempFileName());
                if (tmpFile.exists()) {
                    Log.d(TAG, "Temp file size: " + tmpFile.length());
                    Uri fileUri = FileProvider.getUriForFile(MainActivity.this, AUTHORITY, tmpFile);
                    if (fileUri != null) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(shareIntent);
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
