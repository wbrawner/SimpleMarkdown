package com.wbrawner.simplemarkdown.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter
import com.wbrawner.simplemarkdown.utility.MarkdownObserver
import com.wbrawner.simplemarkdown.utility.ReadabilityObserver
import com.wbrawner.simplemarkdown.view.MarkdownEditView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EditFragment : Fragment(), MarkdownEditView {
    @Inject
    lateinit var presenter: MarkdownPresenter
    private var markdownEditor: EditText? = null
    private var markdownEditorScroller: ScrollView? = null

    private var lastScrollEvent = -1

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit, container, false)
        markdownEditor = view.findViewById(R.id.markdown_edit)
        markdownEditorScroller = view.findViewById(R.id.markdown_edit_container)
        val activity = activity
        if (activity != null) {
            (activity.application as MarkdownApplication).component.inject(this)
        }
        val obs = RxTextView.textChanges(markdownEditor!!)
                .debounce(50, TimeUnit.MILLISECONDS)
                .map { it.toString() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        obs.subscribe(MarkdownObserver(presenter, obs))
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val enableReadability = sharedPrefs.getBoolean(getString(R.string.readability_enabled), false)
        if (enableReadability) {
            val readabilityObserver = RxTextView.textChanges(markdownEditor!!)
                    .debounce(250, TimeUnit.MILLISECONDS)
                    .map { it.toString() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            readabilityObserver.subscribe(ReadabilityObserver(markdownEditor))
        }
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setEditView(this@EditFragment)

        markdownEditorScroller!!.setOnTouchListener { v, event ->
            // The focus should only be set if this was a click, and not a scroll
            if (lastScrollEvent == MotionEvent.ACTION_DOWN && event.action == MotionEvent.ACTION_UP) {
                if (activity == null) {
                    return@setOnTouchListener false
                }
                val imm = activity
                        ?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        ?: return@setOnTouchListener false
                imm.showSoftInput(markdownEditor, InputMethodManager.SHOW_IMPLICIT)
                markdownEditor!!.requestFocus()
            }
            lastScrollEvent = event.action
            false
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setEditView(this)
        markdown = presenter.markdown
    }

    override fun onPause() {
        super.onPause()
        presenter.setEditView(null)
    }

    override fun getMarkdown(): String {
        return markdownEditor!!.text.toString()
    }

    override fun setMarkdown(markdown: String) {
        markdownEditor?.setText(markdown)
    }

    override fun setTitle(title: String) {
        val activity = activity
        if (activity != null) {
            activity.title = title
        }
    }

    override fun onFileSaved(success: Boolean) {
        val message: String = if (success) {
            getString(R.string.file_saved, presenter.fileName)
        } else {
            getString(R.string.file_save_error)
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onFileLoaded(success: Boolean) {
        // TODO: Investigate why this fires off so often
        //        int message = success ? R.string.file_loaded : R.string.file_load_error;
        //        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}// Required empty public constructor
