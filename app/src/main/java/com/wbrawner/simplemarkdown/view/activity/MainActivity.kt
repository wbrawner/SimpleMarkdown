package com.wbrawner.simplemarkdown.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.crashlytics.android.Crashlytics
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter
import com.wbrawner.simplemarkdown.utility.Constants
import com.wbrawner.simplemarkdown.utility.Utils
import com.wbrawner.simplemarkdown.view.adapter.EditPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    @Inject
    lateinit var presenter: MarkdownPresenter

    private var shouldAutoSave = true
    private var newFileHandler: NewFileHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
//            setOnApplyWindowInsetsListener { v, insets ->
//                pager.updatePadding(top = insets.systemWindowInsetTop)
//                insets
//            }
        }
        (application as MarkdownApplication).component.inject(this)
        pager.adapter = EditPagerAdapter(supportFragmentManager, this@MainActivity)
        pager.pageMargin = 1
        pager.setPageMarginDrawable(R.color.colorAccent)
        tabLayout.setupWithViewPager(pager)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tabLayout!!.visibility = View.GONE
        }
        newFileHandler = NewFileHandler()
        if (intent.getBooleanExtra(Constants.EXTRA_EXPLORER, false)) {
            requestFileOp(Constants.REQUEST_OPEN_FILE)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (shouldAutoSave && presenter.markdown.isNotEmpty() && Utils.isAutosaveEnabled(this)) {
//            presenter.saveMarkdown(null, null)
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
            R.id.action_save -> requestFileOp(Constants.REQUEST_SAVE_FILE)
            R.id.action_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.putExtra(Intent.EXTRA_TEXT, presenter.markdown)
                shareIntent.type = "text/plain"
                startActivity(Intent.createChooser(
                        shareIntent,
                        getString(R.string.share_file)
                ))
            }
            R.id.action_load -> requestFileOp(Constants.REQUEST_OPEN_FILE)
//            R.id.action_new -> presenter.saveMarkdown(newFileHandler, null)
            R.id.action_lock_swipe -> {
                item.isChecked = !item.isChecked
                pager!!.setSwipeLocked(item.isChecked)
            }
            R.id.action_help -> showInfoActivity(R.id.action_help)
            R.id.action_settings -> {
                val settingsIntent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(settingsIntent)
            }
            R.id.action_libraries -> showInfoActivity(R.id.action_libraries)
            R.id.action_privacy -> showInfoActivity(R.id.action_privacy)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showInfoActivity(action: Int) {
        val infoIntent = Intent(this@MainActivity, MarkdownInfoActivity::class.java)
        var fileName = ""
        var title = ""
        when (action) {
            R.id.action_help -> {
                fileName = "Cheatsheet.md"
                title = getString(R.string.action_help)
            }
            R.id.action_libraries -> {
                fileName = "Libraries.md"
                title = getString(R.string.action_libraries)
            }
            R.id.action_privacy -> {
                fileName = "Privacy Policy.md"
                title = getString(R.string.action_privacy)
            }
        }
        infoIntent.putExtra("title", title)
        var `in`: InputStream? = null
        try {
            val assetManager = assets
            if (assetManager != null) {
                `in` = assetManager.open(fileName)
            }
            presenter.loadMarkdown(fileName, `in`, object : MarkdownPresenter.OnTempFileLoadedListener {
                override fun onSuccess(html: String) {
                    infoIntent.putExtra("html", html)
                    startActivity(infoIntent)
                }

                override fun onError() {
                    Toast.makeText(this@MainActivity, R.string.file_load_error, Toast.LENGTH_SHORT)
                            .show()
                }
            })
        } catch (e: Exception) {
            Crashlytics.logException(e)
            Toast.makeText(this@MainActivity, R.string.file_load_error, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.REQUEST_SAVE_FILE, Constants.REQUEST_OPEN_FILE -> {
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
            Constants.REQUEST_OPEN_FILE -> {
                if (resultCode != Activity.RESULT_OK || data?.data == null) {
                    return
                }

                val mimeType: String? = data.data?.let { returnUri ->
                    contentResolver.getType(returnUri)
                }

                val fileName = contentResolver.query(data.data!!, null, null, null, null)
                        ?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            cursor.getString(nameIndex)
                        } ?: "Untitled.md"

                contentResolver.openFileDescriptor(data.data!!, "r")?.let {
                    val fileInput = FileInputStream(it.fileDescriptor)
                    presenter.loadMarkdown(fileName, fileInput)
                }
            }
            Constants.REQUEST_SAVE_FILE -> {
                if (resultCode != Activity.RESULT_OK
                        || data?.data == null) {
                    return
                }

                val fileName = contentResolver.query(data.data!!, null, null, null, null)
                        ?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            cursor.getString(nameIndex)
                        } ?: "Untitled.md"

                presenter.saveMarkdown(
                        null,
                        fileName,
                        contentResolver.openOutputStream(data.data!!)
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun requestFileOp(requestType: Int) {
        if (!Utils.canAccessFiles(this@MainActivity)) {
            if (Build.VERSION.SDK_INT < 23) return
            requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    requestType
            )
            return
        }
        // If the user is going to save the file, we don't want to auto-save it for them
        shouldAutoSave = false
        val intent = when (requestType) {
            Constants.REQUEST_SAVE_FILE -> {
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "text/markdown"
                    putExtra(Intent.EXTRA_TITLE, presenter.fileName)
                }
            }
            Constants.REQUEST_OPEN_FILE -> {
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "text/markdown"))
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
        title = presenter.fileName
        shouldAutoSave = true
    }

    private inner class NewFileHandler : MarkdownPresenter.MarkdownSavedListener {
        override fun saveComplete(success: Boolean) {
            if (success) {
                val newFile = Utils.getDefaultFileName(this@MainActivity)
                presenter.newFile(newFile)
            } else {
                Toast.makeText(
                        this@MainActivity,
                        R.string.file_save_error,
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
