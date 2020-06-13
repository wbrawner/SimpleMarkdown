package com.wbrawner.simplemarkdown.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.view.adapter.EditPagerAdapter
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class MainFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback, CoroutineScope {

    private var shouldAutoSave = true
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val viewModel: MarkdownViewModel by activityViewModels()
    private var appBarConfiguration: AppBarConfiguration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(findNavController()) {
            appBarConfiguration = AppBarConfiguration(graph, drawerLayout)
            toolbar.setupWithNavController(this, appBarConfiguration!!)
            toolbar.inflateMenu(R.menu.menu_edit)
            toolbar.setOnMenuItemClickListener { item ->
                return@setOnMenuItemClickListener when (item.itemId) {
                    R.id.action_save -> {
                        launch {
                            if (!viewModel.save(requireContext())) {
                                requestFileOp(REQUEST_SAVE_FILE)
                            } else {
                                Toast.makeText(
                                        requireContext(),
                                        getString(R.string.file_saved, viewModel.fileName.value),
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        true
                    }
                    R.id.action_save_as -> {
                        requestFileOp(REQUEST_SAVE_FILE)
                        true
                    }
                    R.id.action_share -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, viewModel.markdownUpdates.value)
                        shareIntent.type = "text/plain"
                        startActivity(Intent.createChooser(
                                shareIntent,
                                getString(R.string.share_file)
                        ))
                        true
                    }
                    R.id.action_load -> {
                        requestFileOp(REQUEST_OPEN_FILE)
                        true
                    }
                    R.id.action_new -> {
                        promptSaveOrDiscardChanges()
                        true
                    }
                    R.id.action_lock_swipe -> {
                        item.isChecked = !item.isChecked
                        pager!!.setSwipeLocked(item.isChecked)
                        true
                    }
                    else -> item.onNavDestinationSelected(findNavController())
                }
            }
            navigationView.setupWithNavController(this)
        }
        val adapter = EditPagerAdapter(childFragmentManager, view.context)
        pager.adapter = adapter
        pager.addOnPageChangeListener(adapter)
        pager.pageMargin = 1
        pager.setPageMarginDrawable(R.color.colorAccent)
        tabLayout.setupWithViewPager(pager)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tabLayout!!.visibility = View.GONE
        }
        @Suppress("CAST_NEVER_SUCCEEDS")
        viewModel.fileName.observe(viewLifecycleOwner, Observer {
            toolbar?.title = it
        })
    }

    override fun onStart() {
        super.onStart()
        launch {
            withContext(Dispatchers.IO) {
                val enableErrorReports = PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getBoolean(getString(R.string.error_reports_enabled), true)
                (requireActivity().application as MarkdownApplication).errorHandler.enable(enableErrorReports)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        launch {
            val context = context?.applicationContext ?: return@launch
            withContext(Dispatchers.IO) {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                val isAutoSaveEnabled = sharedPrefs.getBoolean(KEY_AUTOSAVE, true)
                if (!shouldAutoSave || !isAutoSaveEnabled) {
                    return@withContext
                }

                val uri = if (viewModel.save(context)) {
                    viewModel.uri.value
                } else {
                    // The user has left the app, with autosave enabled, and we don't already have a
                    // Uri for them or for some reason we were unable to save to the original Uri. In
                    // this case, we need to just save to internal file storage so that we can recover
                    val fileUri = Uri.fromFile(File(context.filesDir, viewModel.fileName.value!!))
                    if (viewModel.save(context, fileUri)) {
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
                    context?.let {
                        Toast.makeText(it, R.string.no_permissions, Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_OPEN_FILE -> {
                if (resultCode != Activity.RESULT_OK || data?.data == null) {
                    return
                }

                launch {
                    val fileLoaded = context?.let {
                        viewModel.load(it, data.data)
                    }
                    if (fileLoaded == false) {
                        context?.let {
                            Toast.makeText(it, R.string.file_load_error, Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }
                }
            }
            REQUEST_SAVE_FILE -> {
                if (resultCode != Activity.RESULT_OK || data?.data == null) {
                    return
                }

                launch {
                    context?.let {
                        viewModel.save(it, data.data)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun promptSaveOrDiscardChanges() {
        if (viewModel.originalMarkdown.value == viewModel.markdownUpdates.value) {
            viewModel.reset("Untitled.md")
            return
        }
        val context = context ?: return
        AlertDialog.Builder(context)
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
        val context = context ?: return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
