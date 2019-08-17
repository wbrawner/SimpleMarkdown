package com.wbrawner.simplemarkdown.view.fragment

import android.content.SharedPreferences
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
import com.wbrawner.simplemarkdown.BuildConfig
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter
import com.wbrawner.simplemarkdown.presentation.MarkdownPreviewView
import javax.inject.Inject


class PreviewFragment : Fragment(), MarkdownPreviewView {
    @Inject
    lateinit var presenter: MarkdownPresenter
    private var markdownPreview: WebView? = null
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.context)
        markdownPreview = view.findViewById(R.id.markdown_view)
        (activity?.application as? MarkdownApplication)?.component?.inject(this)
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }

    override fun updatePreview(html: String) {
        markdownPreview?.post {
            val isNightMode = AppCompatDelegate.getDefaultNightMode() ==
                    AppCompatDelegate.MODE_NIGHT_YES
                    || context!!.resources.configuration.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
            val defaultCssId = if (isNightMode) {
                R.string.pref_custom_css_default_dark
            } else {
                R.string.pref_custom_css_default
            }
            @Suppress("ConstantConditionIf")
            val css: String? = if (!BuildConfig.ENABLE_CUSTOM_CSS) {
                context?.getString(defaultCssId)
            } else {
                sharedPreferences!!.getString(
                        getString(R.string.pref_custom_css),
                        getString(defaultCssId)
                )
            }

            val style = String.format(FORMAT_CSS, css)

            markdownPreview?.loadDataWithBaseURL(null,
                    style + html,
                    "text/html",
                    "UTF-8", null
            )
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.previewView = this
        presenter.onMarkdownEdited()
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
        super.onDestroy()
        presenter.previewView = null
    }

    companion object {
        var FORMAT_CSS = "<style>" +
                "%s" +
                "</style>"
    }
}
