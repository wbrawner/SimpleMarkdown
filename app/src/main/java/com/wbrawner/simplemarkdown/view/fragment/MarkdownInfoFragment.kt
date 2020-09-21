package com.wbrawner.simplemarkdown.view.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.ErrorHandler
import com.wbrawner.simplemarkdown.utility.errorHandlerImpl
import com.wbrawner.simplemarkdown.utility.readAssetToString
import com.wbrawner.simplemarkdown.utility.toHtml
import kotlinx.android.synthetic.main.fragment_markdown_info.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MarkdownInfoFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val errorHandler: ErrorHandler by errorHandlerImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_markdown_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fileName = arguments?.getString(EXTRA_FILE)
        if (fileName.isNullOrBlank()) {
            findNavController().navigateUp()
            return
        }
        toolbar.setupWithNavController(findNavController())

        val isNightMode = AppCompatDelegate.getDefaultNightMode() ==
                AppCompatDelegate.MODE_NIGHT_YES
                || resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val defaultCssId = if (isNightMode) {
            R.string.pref_custom_css_default_dark
        } else {
            R.string.pref_custom_css_default
        }
        val css: String? = getString(defaultCssId)
        launch {
            try {
                val html = view.context.assets?.readAssetToString(fileName)
                        ?.toHtml()
                        ?: throw RuntimeException("Unable to open stream to $fileName")
                infoWebview.loadDataWithBaseURL(null,
                        String.format(FORMAT_CSS, css) + html,
                        "text/html",
                        "UTF-8", null
                )
            } catch (e: Exception) {
                errorHandler.reportException(e)
                Toast.makeText(view.context, R.string.file_load_error, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroy() {
        coroutineContext[Job]?.cancel()
        super.onDestroy()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val FORMAT_CSS = "<style>" +
                "%s" +
                "</style>"
        const val EXTRA_TITLE = "title"
        const val EXTRA_FILE = "file"
    }
}
