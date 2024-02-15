//package com.wbrawner.simplemarkdown.view.fragment
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.content.res.Configuration
//import android.os.Build
//import android.os.Bundle
//import android.view.*
//import android.webkit.MimeTypeMap
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.setupWithNavController
//import androidx.preference.PreferenceManager
//import com.wbrawner.plausible.android.Plausible
//import com.wbrawner.simplemarkdown.R
//import com.wbrawner.simplemarkdown.databinding.FragmentMainBinding
//import com.wbrawner.simplemarkdown.utility.ErrorHandler
//import com.wbrawner.simplemarkdown.utility.errorHandlerImpl
//import com.wbrawner.simplemarkdown.view.adapter.EditPagerAdapter
//import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import timber.log.Timber
//
//class MainFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {
//
//    private val viewModel: MarkdownViewModel by viewModels()
//    private var appBarConfiguration: AppBarConfiguration? = null
//    private val errorHandler: ErrorHandler by errorHandlerImpl()
//    private var _binding: FragmentMainBinding? = null
//    private val binding: FragmentMainBinding
//        get() = _binding!!
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context !is Activity) return
//        lifecycleScope.launch {
//            viewModel.load(context, context.intent?.data)
//            context.intent?.data = null
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_edit, menu)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            menu.findItem(R.id.action_save_as)
//                ?.setAlphabeticShortcut('S', KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentMainBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        with(findNavController()) {
//            appBarConfiguration = AppBarConfiguration(graph, binding.drawerLayout)
//            binding.toolbar.setupWithNavController(this, appBarConfiguration!!)
//            (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
//            binding.navigationView.setupWithNavController(this)
//        }
//        val adapter = EditPagerAdapter(childFragmentManager, view.context)
//        binding.pager.adapter = adapter
//        binding.pager.addOnPageChangeListener(adapter)
//        binding.pager.pageMargin = 1
//        binding.pager.setPageMarginDrawable(R.color.colorAccent)
//        binding.tabLayout.setupWithViewPager(binding.pager)
//        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            binding.tabLayout.visibility = View.GONE
//        }
//        @Suppress("CAST_NEVER_SUCCEEDS")
//        viewModel.fileName.observe(viewLifecycleOwner) {
//            binding.toolbar.title = it
//        }
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                binding.drawerLayout.open()
//                true
//            }
//
//            R.id.action_save -> {
//                Timber.d("Save clicked")
//                lifecycleScope.launch {
//                    if (!viewModel.save(requireContext())) {
//                        requestFileOp(REQUEST_SAVE_FILE)
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            getString(R.string.file_saved, viewModel.fileName.value),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                true
//            }
//
//            R.id.action_save_as -> {
//                Timber.d("Save as clicked")
//                requestFileOp(REQUEST_SAVE_FILE)
//                true
//            }
//
//            R.id.action_share -> {
//                Timber.d("Share clicked")
//                val shareIntent = Intent(Intent.ACTION_SEND)
//                shareIntent.putExtra(Intent.EXTRA_TEXT, viewModel.markdownUpdates.value)
//                shareIntent.type = "text/plain"
//                startActivity(
//                    Intent.createChooser(
//                        shareIntent,
//                        getString(R.string.share_file)
//                    )
//                )
//                true
//            }
//
//            R.id.action_load -> {
//                Timber.d("Load clicked")
//                requestFileOp(REQUEST_OPEN_FILE)
//                true
//            }
//
//            R.id.action_new -> {
//                Timber.d("New clicked")
//                promptSaveOrDiscardChanges()
//                true
//            }
//
//            R.id.action_lock_swipe -> {
//                Timber.d("Lock swiping clicked")
//                item.isChecked = !item.isChecked
//                binding.pager.setSwipeLocked(item.isChecked)
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        Plausible.pageView("")
//        lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
//                val enableErrorReports =
//                    PreferenceManager.getDefaultSharedPreferences(requireContext())
//                        .getBoolean(getString(R.string.pref_key_error_reports_enabled), true)
//                Timber.d("MainFragment started. Error reports enabled? $enableErrorReports")
//                errorHandler.enable(enableErrorReports)
//            }
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        val context = context?.applicationContext ?: return
//        lifecycleScope.launch {
//            viewModel.autosave(context, PreferenceManager.getDefaultSharedPreferences(context))
//        }
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Timber.d("Orientation changed to landscape, hiding tabs")
//            binding.tabLayout.visibility = View.GONE
//        } else {
//            Timber.d("Orientation changed to portrait, showing tabs")
//            binding.tabLayout.visibility = View.VISIBLE
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            REQUEST_SAVE_FILE, REQUEST_OPEN_FILE -> {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission granted, open file save dialog
//                    Timber.d("Storage permissions granted")
//                    requestFileOp(requestCode)
//                } else {
//                    // Permission denied, do nothing
//                    Timber.d("Storage permissions denied, unable to save or load files")
//                    context?.let {
//                        Toast.makeText(it, R.string.no_permissions, Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        when (requestCode) {
//            REQUEST_OPEN_FILE -> {
//                if (resultCode != Activity.RESULT_OK || data?.data == null) {
//                    Timber.w(
//                        "Unable to open file. Result ok? %b Intent uri: %s",
//                        resultCode == Activity.RESULT_OK,
//                        data?.data?.toString()
//                    )
//                    return
//                }
//
//                lifecycleScope.launch {
//                    context?.let {
//                        if (!viewModel.load(it, data.data)) {
//                            Toast.makeText(it, R.string.file_load_error, Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            }
//
//            REQUEST_SAVE_FILE -> {
//                if (resultCode != Activity.RESULT_OK || data?.data == null) {
//                    Timber.w(
//                        "Unable to save file. Result ok? %b Intent uri: %s",
//                        resultCode == Activity.RESULT_OK,
//                        data?.data?.toString()
//                    )
//                    return
//                }
//
//                lifecycleScope.launch {
//                    context?.let {
//                        viewModel.save(it, data.data)
//                    }
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//
//    private fun promptSaveOrDiscardChanges() {
//        if (!viewModel.shouldPromptSave()) {
//            viewModel.reset(
//                "Untitled.md",
//                PreferenceManager.getDefaultSharedPreferences(requireContext())
//            )
//            return
//        }
//        val context = context ?: run {
//            Timber.w("Context is null, unable to show prompt for save or discard")
//            return
//        }
//        AlertDialog.Builder(context)
//            .setTitle(R.string.save_changes)
//            .setMessage(R.string.prompt_save_changes)
//            .setNegativeButton(R.string.action_discard) { _, _ ->
//                Timber.d("Discarding changes")
//                viewModel.reset(
//                    "Untitled.md",
//                    PreferenceManager.getDefaultSharedPreferences(requireContext())
//                )
//            }
//            .setPositiveButton(R.string.action_save) { _, _ ->
//                Timber.d("Saving changes")
//                requestFileOp(REQUEST_SAVE_FILE)
//            }
//            .create()
//            .show()
//    }
//
//    private fun requestFileOp(requestType: Int) {
//        val intent = when (requestType) {
//            REQUEST_SAVE_FILE -> {
//                Timber.d("Requesting save op")
//                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//                    type = "text/markdown"
//                    putExtra(Intent.EXTRA_TITLE, viewModel.fileName.value)
//                }
//            }
//
//            REQUEST_OPEN_FILE -> {
//                Timber.d("Requesting open op")
//                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                    type = "*/*"
//                    if (MimeTypeMap.getSingleton().hasMimeType("md")) {
//                        // If the device doesn't recognize markdown files then we're not going to be
//                        // able to open them at all, so there's no sense in filtering them out.
//                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "text/markdown"))
//                    }
//                }
//            }
//
//            else -> {
//                Timber.w("Ignoring unknown file op request: $requestType")
//                null
//            }
//        } ?: return
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        startActivityForResult(
//            intent,
//            requestType
//        )
//    }
//
//    companion object {
//        // Request codes
//        const val REQUEST_OPEN_FILE = 1
//        const val REQUEST_SAVE_FILE = 2
//    }
//}
