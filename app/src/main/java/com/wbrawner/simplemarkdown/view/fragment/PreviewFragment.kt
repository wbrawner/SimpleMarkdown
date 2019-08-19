package com.wbrawner.simplemarkdown.view.fragment

import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.wbrawner.simplemarkdown.BuildConfig
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.toHtml
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PreviewFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    lateinit var viewModel: MarkdownViewModel
    private var markdownPreview: WebView? = null
    private var style: String = ""

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        markdownPreview = view.findViewById(R.id.markdown_view)
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(
                this,
                (requireActivity().application as MarkdownApplication).viewModelFactory
        ).get(MarkdownViewModel::class.java)
        launch {
            val isNightMode = AppCompatDelegate.getDefaultNightMode() ==
                    AppCompatDelegate.MODE_NIGHT_YES
                    || context!!.resources.configuration.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
            val defaultCssId = if (isNightMode) {
                R.string.pref_custom_css_default_dark
            } else {
                R.string.pref_custom_css_default
            }
            val css = withContext(Dispatchers.IO) {
                @Suppress("ConstantConditionIf")
                if (!BuildConfig.ENABLE_CUSTOM_CSS) {
                    requireActivity().getString(defaultCssId)
                } else {
                    PreferenceManager.getDefaultSharedPreferences(requireActivity())
                            .getString(
                                    getString(R.string.pref_custom_css),
                                    getString(defaultCssId)
                            ) ?: ""
                }
            }
            style = String.format(FORMAT_CSS, css)
            updateWebContent(viewModel.markdownUpdates.value ?: "")
            viewModel.markdownUpdates.observe(this@PreviewFragment, Observer<String> {
                updateWebContent(it)
            })
        }
    }

    private fun updateWebContent(markdown: String) {
        markdownPreview?.post {
            launch {
                markdownPreview?.loadDataWithBaseURL(null,
                        style + markdown.toHtml(),
                        "text/html",
                        "UTF-8", null
                )
            }
        }
    }

    override fun onDestroyView() {
        markdownPreview?.let {
            (it.parent as ViewGroup).removeView(it)
            it.destroy()
            markdownPreview = null
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        coroutineContext[Job]?.let {
            cancel()
        }
        super.onDestroy()
    }

    companion object {
        var FORMAT_CSS = "<style>" +
                "%s" +
                "</style>"
    }
}
