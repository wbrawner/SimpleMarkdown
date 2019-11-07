package com.wbrawner.simplemarkdown.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.view.adapter.EditPagerAdapter
import com.wbrawner.simplemarkdown.view.fragment.MainMenuFragment
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback, CoroutineScope {

    private var shouldAutoSave = true
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private lateinit var viewModel: MarkdownViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
        viewModel = ViewModelProviders.of(
                this,
                (application as MarkdownApplication).viewModelFactory
        ).get(MarkdownViewModel::class.java)
        val adapter = EditPagerAdapter(supportFragmentManager, this@MainActivity)
        pager.adapter = adapter
        pager.addOnPageChangeListener(adapter)
        pager.pageMargin = 1
        pager.setPageMarginDrawable(R.color.colorAccent)
        tabLayout.setupWithViewPager(pager)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tabLayout!!.visibility = View.GONE
        }
        @Suppress("CAST_NEVER_SUCCEEDS")
        viewModel.fileName.observe(this, Observer<String> {
            title = it
        })
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        launch {
            withContext(Dispatchers.IO) {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                val isAutoSaveEnabled = sharedPrefs.getBoolean(KEY_AUTOSAVE, true)
                if (!shouldAutoSave || !isAutoSaveEnabled) {
                    return@withContext
                }

                val uri = if (viewModel.save(this@MainActivity)) {
                    viewModel.uri.value
                } else {
                    // The user has left the app, with autosave enabled, and we don't already have a
                    // Uri for them or for some reason we were unable to save to the original Uri. In
                    // this case, we need to just save to internal file storage so that we can recover
                    val fileUri = Uri.fromFile(File(filesDir, viewModel.fileName.value!!))
                    if (viewModel.save(this@MainActivity, fileUri)) {
                        fileUri
                    } else {
                        null
                    }
                } ?: return@withContext
                sharedPrefs.edit()
                        .putString(getString(R.string.pref_key_autosave_uri), uri.toString())
                        .apply()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            tabLayout!!.visibility = View.GONE
        else
            tabLayout!!.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                MainMenuFragment()
                        .apply {
                            errorHandler = (application as MarkdownApplication).errorHandler
                        }
                        .show(supportFragmentManager, null)
            }
            R.id.action_save -> {
                launch {
                    if (!viewModel.save(this@MainActivity)) {
                        requestFileOp(REQUEST_SAVE_FILE)
                    } else {
                        Toast.makeText(
                                this@MainActivity,
                                getString(R.string.file_saved, viewModel.fileName.value),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            R.id.action_save_as -> {
                requestFileOp(REQUEST_SAVE_FILE)
            }
            R.id.action_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.putExtra(Intent.EXTRA_TEXT, viewModel.markdownUpdates.value)
                shareIntent.type = "text/plain"
                startActivity(Intent.createChooser(
                        shareIntent,
                        getString(R.string.share_file)
                ))
            }
            R.id.action_load -> requestFileOp(REQUEST_OPEN_FILE)
            R.id.action_new -> promptSaveOrDiscardChanges()
            R.id.action_lock_swipe -> {
                item.isChecked = !item.isChecked
                pager!!.setSwipeLocked(item.isChecked)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_SAVE_FILE, REQUEST_OPEN_FILE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, open file save dialog
                    requestFileOp(requestCode)
                } else {
                    // Permission denied, do nothing
                    Toast.makeText(this@MainActivity, R.string.no_permissions, Toast.LENGTH_SHORT)
                            .show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (pager!!.currentItem == EditPagerAdapter.FRAGMENT_EDIT)
            super.onBackPressed()
        else
            pager!!.currentItem = EditPagerAdapter.FRAGMENT_EDIT
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_OPEN_FILE -> {
                if (resultCode != Activity.RESULT_OK || data?.data == null) {
                    return
                }

                launch {
                    val fileLoaded = viewModel.load(this@MainActivity, data.data)
                    if (!fileLoaded) {
                        Toast.makeText(this@MainActivity, R.string.file_load_error, Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
            REQUEST_SAVE_FILE -> {
                if (resultCode != Activity.RESULT_OK || data?.data == null) {
                    return
                }

                launch {
                    viewModel.save(this@MainActivity, data.data)
                }
            }
            REQUEST_DARK_MODE -> recreate()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun promptSaveOrDiscardChanges() {
        if (viewModel.originalMarkdown.value == viewModel.markdownUpdates.value) {
            viewModel.reset("Untitled.md")
            return
        }
        AlertDialog.Builder(this)
                .setTitle(R.string.save_changes)
                .setMessage(R.string.prompt_save_changes)
                .setNegativeButton(R.string.action_discard) { _, _ ->
                    viewModel.reset("Untitled.md")
                }
                .setPositiveButton(R.string.action_save) { _, _ ->
                    requestFileOp(REQUEST_SAVE_FILE)
                }
                .create()
                .show()
    }

    private fun requestFileOp(requestType: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT > 22) {
            requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    requestType
            )
            return
        }
        // If the user is going to save the file, we don't want to auto-save it for them
        shouldAutoSave = false
        val intent = when (requestType) {
            REQUEST_SAVE_FILE -> {
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "text/markdown"
                    putExtra(Intent.EXTRA_TITLE, viewModel.fileName.value)
                }
            }
            REQUEST_OPEN_FILE -> {
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    if (MimeTypeMap.getSingleton().hasMimeType("md")) {
                        // If the device doesn't recognize markdown files then we're not going to be
                        // able to open them at all, so there's no sense in filtering them out.
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "text/markdown"))
                    }
                }
            }
            else -> null
        } ?: return
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
                intent,
                requestType
        )
    }

    override fun onResume() {
        super.onResume()
        shouldAutoSave = true
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext[Job]?.let {
            cancel()
        }
    }

    companion object {
        // Request codes
        const val REQUEST_OPEN_FILE = 1
        const val REQUEST_SAVE_FILE = 2
        const val REQUEST_DARK_MODE = 4
        const val KEY_AUTOSAVE = "autosave"
    }
}
