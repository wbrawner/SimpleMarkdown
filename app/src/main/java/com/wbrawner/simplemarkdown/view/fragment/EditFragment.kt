package com.wbrawner.simplemarkdown.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.wbrawner.simplemarkdown.utility.hideKeyboard
import com.wbrawner.simplemarkdown.utility.showKeyboard
import com.wbrawner.simplemarkdown.view.MarkdownEditView
import com.wbrawner.simplemarkdown.view.ViewPagerPage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class EditFragment : Fragment(), MarkdownEditView, ViewPagerPage {
    @Inject
    lateinit var presenter: MarkdownPresenter
    private var markdownEditor: EditText? = null
    private var markdownEditorScroller: ScrollView? = null

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

        var touchDown = 0L
        var oldX = 0f
        var oldY = 0f
        markdownEditorScroller!!.setOnTouchListener { _, event ->
            // The ScrollView's onClickListener doesn't seem to be called, so I've had to
            // implement a sort of custom click listener that checks that the tap was both quick
            // and didn't drag.
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDown = System.currentTimeMillis()
                    oldX = event.rawX
                    oldY = event.rawY
                }
                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - touchDown < 150
                            && abs(event.rawX - oldX) < 25
                            && abs(event.rawY - oldY) < 25)
                        markdownEditor?.showKeyboard()
                }
            }
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
        markdownEditor?.hideKeyboard()
    }

    override fun onSelected() {
        markdownEditor?.showKeyboard()
    }

    override fun onDeselected() {
        markdownEditor?.hideKeyboard()
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
