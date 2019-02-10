package com.wbrawner.simplemarkdown.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.wbrawner.simplemarkdown.MarkdownApplication;
import com.wbrawner.simplemarkdown.R;
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter;
import com.wbrawner.simplemarkdown.utility.Constants;
import com.wbrawner.simplemarkdown.utility.Utils;
import com.wbrawner.simplemarkdown.view.DisableableViewPager;
import com.wbrawner.simplemarkdown.view.adapter.EditPagerAdapter;

import org.acra.ACRA;

import java.io.File;
import java.io.InputStream;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Inject
    MarkdownPresenter presenter;

    @BindView(R.id.pager)
    DisableableViewPager pager;
    @BindView(R.id.layout_tab)
    TabLayout tabLayout;
    @BindView(R.id.adView)
    AdView adView;

    private boolean shouldAutoSave = true;
    private NewFileHandler newFileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((MarkdownApplication) getApplication()).getComponent().inject(this);
        ButterKnife.bind(this);
        pager.setAdapter(
                new EditPagerAdapter(getSupportFragmentManager(), MainActivity.this)
        );
        pager.setPageMargin(1);
        pager.setPageMarginDrawable(R.color.colorAccent);
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            tabLayout.setVisibility(View.GONE);
        }
        newFileHandler = new NewFileHandler();
        if (getIntent().getBooleanExtra(Constants.EXTRA_EXPLORER, false)) {
            requestFileOp(Constants.REQUEST_OPEN_FILE);
        }

        // TODO: Change this to only load for users who haven't paid to remove ads
        if (true) {
            MobileAds.initialize(this, getString(R.string.admob_app_id));
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("7045AFE2F8D6C6386983BAF3D2A2377E")
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    ACRA.getErrorReporter().handleException(new RuntimeException("Failed to load ads: " + i), false);
                }
            });
            adView.loadAd(adRequest);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (shouldAutoSave && !presenter.getMarkdown().isEmpty() && Utils.isAutosaveEnabled(this)) {
            presenter.saveMarkdown(null, null);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            tabLayout.setVisibility(View.GONE);
        else
            tabLayout.setVisibility(View.VISIBLE);
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
                requestFileOp(Constants.REQUEST_SAVE_FILE);
                break;
            case R.id.action_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, presenter.getMarkdown());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(
                        shareIntent,
                        getString(R.string.share_file)
                ));
                break;
            case R.id.action_load:
                requestFileOp(Constants.REQUEST_OPEN_FILE);
                break;
            case R.id.action_new:
                presenter.saveMarkdown(newFileHandler, null);
                break;
            case R.id.action_lock_swipe:
                item.setChecked(!item.isChecked());
                pager.setSwipeLocked(item.isChecked());
                break;
            case R.id.action_help:
                showInfoActivity(R.id.action_help);
                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_libraries:
                showInfoActivity(R.id.action_libraries);
                break;
            case R.id.action_privacy:
                showInfoActivity(R.id.action_privacy);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfoActivity(int action) {
        Intent infoIntent = new Intent(MainActivity.this, MarkdownInfoActivity.class);
        String fileName = "";
        String title = "";
        switch (action) {
            case R.id.action_help:
                fileName = "Cheatsheet.md";
                title = getString(R.string.action_help);
                break;
            case R.id.action_libraries:
                fileName = "Libraries.md";
                title = getString(R.string.action_libraries);
                break;
            case R.id.action_privacy:
                fileName = "Privacy Policy.md";
                title = getString(R.string.action_privacy);
                break;
        }
        infoIntent.putExtra("title", title);
        InputStream in = null;
        try {
            AssetManager assetManager = getAssets();
            if (assetManager != null) {
                in = assetManager.open(fileName);
            }
            presenter.loadMarkdown(fileName, in, new MarkdownPresenter.OnTempFileLoadedListener() {
                @Override
                public void onSuccess(String html) {
                    infoIntent.putExtra("html", html);
                    startActivity(infoIntent);
                }

                @Override
                public void onError() {
                    Toast.makeText(MainActivity.this, R.string.file_load_error, Toast.LENGTH_SHORT)
                            .show();
                }
            });
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(e, false);
            Toast.makeText(MainActivity.this, R.string.file_load_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults
    ) {
        switch (requestCode) {
            case Constants.REQUEST_SAVE_FILE:
            case Constants.REQUEST_OPEN_FILE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, open file save dialog
                    requestFileOp(requestCode);
                } else {
                    // Permission denied, do nothing
                    Toast.makeText(MainActivity.this, R.string.no_permissions, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == EditPagerAdapter.FRAGMENT_EDIT)
            super.onBackPressed();
        else
            pager.setCurrentItem(EditPagerAdapter.FRAGMENT_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_OPEN_FILE:
                if (resultCode != RESULT_OK || data == null || !data.hasExtra(Constants.EXTRA_FILE)) {
                    break;
                }

                File markdownFile = (File) data.getSerializableExtra(Constants.EXTRA_FILE);
                presenter.loadMarkdown(markdownFile);
                break;
            case Constants.REQUEST_SAVE_FILE:
                if (resultCode != RESULT_OK
                        || data == null
                        || !data.hasExtra(Constants.EXTRA_FILE_PATH)
                        || data.getStringExtra(Constants.EXTRA_FILE_PATH).isEmpty()) {
                    break;
                }
                String path = data.getStringExtra(Constants.EXTRA_FILE_PATH);
                presenter.saveMarkdown(null, path);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestFileOp(int requestType) {
        if (Utils.canAccessFiles(MainActivity.this)) {
            // If the user is going to save the file, we don't want to auto-save it for them
            shouldAutoSave = false;
            Intent intent = new Intent(MainActivity.this, ExplorerActivity.class);
            intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestType);
            intent.putExtra(Constants.EXTRA_FILE, presenter.getFile());
            startActivityForResult(
                    intent,
                    requestType
            );
        } else if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestType
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(presenter.getFileName());
        shouldAutoSave = true;
    }

    private class NewFileHandler implements MarkdownPresenter.MarkdownSavedListener {
        @Override
        public void saveComplete(boolean success) {
            if (success) {
                String newFile = Utils.getDefaultFileName(MainActivity.this);
                presenter.newFile(newFile);
            } else {
                Toast.makeText(
                        MainActivity.this,
                        R.string.file_save_error,
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
