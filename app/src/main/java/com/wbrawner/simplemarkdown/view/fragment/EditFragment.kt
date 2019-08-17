package com.wbrawner.simplemarkdown.view.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.wbrawner.simplemarkdown.MarkdownApplication
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.model.Readability
import com.wbrawner.simplemarkdown.presentation.MarkdownEditView
import com.wbrawner.simplemarkdown.presentation.MarkdownPresenter
import com.wbrawner.simplemarkdown.utility.hideKeyboard
import com.wbrawner.simplemarkdown.utility.showKeyboard
import com.wbrawner.simplemarkdown.view.ViewPagerPage
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class EditFragment : Fragment(), MarkdownEditView, ViewPagerPage, CoroutineScope {
    @Inject
    lateinit var presenter: MarkdownPresenter
    private var markdownEditor: EditText? = null
    private var markdownEditorScroller: ScrollView? = null
    override var markdown: String
        get() = markdownEditor?.text?.toString() ?: ""
        set(value) {
            markdownEditor?.setText(value)
        }
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_edit, container, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        markdownEditor = view.findViewById(R.id.markdown_edit)
        markdownEditorScroller = view.findViewById(R.id.markdown_edit_container)
        markdownEditor?.addTextChangedListener(object : TextWatcher {
            private var searchFor = ""

            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                if (searchText == searchFor)
                    return

                searchFor = searchText

                launch {
                    delay(50)
                    if (searchText != searchFor)
                        return@launch
                    presenter.onMarkdownEdited(searchText)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val enableReadability = sharedPrefs.getBoolean(getString(R.string.readability_enabled), false)
        if (enableReadability) {
            markdownEditor?.addTextChangedListener(object : TextWatcher {
                private var previousValue = ""
                private var searchFor = ""

                override fun afterTextChanged(s: Editable?) {
                    val searchText = s.toString().trim()
                    if (searchText == searchFor)
                        return

                    searchFor = searchText

                    launch {
                        delay(250)
                        if (searchText != searchFor)
                            return@launch
                        val start = System.currentTimeMillis()
                        if (markdown.isEmpty()) return@launch
                        if (previousValue == markdown) return@launch
                        val readability = Readability(markdown)
                        val span = SpannableString(markdown)
                        for (sentence in readability.sentences()) {
                            var color = Color.TRANSPARENT
                            if (sentence.syllableCount() > 25) color = Color.argb(100, 229, 232, 42)
                            if (sentence.syllableCount() > 35) color = Color.argb(100, 193, 66, 66)
                            span.setSpan(BackgroundColorSpan(color), sentence.start(), sentence.end(), 0)
                        }
                        markdownEditor?.setTextKeepState(span, TextView.BufferType.SPANNABLE)
                        previousValue = markdown
                        val timeTakenMs = System.currentTimeMillis() - start
                        Log.d("SimpleMarkdown", "Handled markdown in " + timeTakenMs + "ms")
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
        }

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        @Suppress("CAST_NEVER_SUCCEEDS")
        (activity?.application as? MarkdownApplication)?.component?.inject(this)
        presenter.editView = this@EditFragment
    }

    override fun onResume() {
        super.onResume()
        presenter.editView = this
        markdown = presenter.markdown
    }

    override fun onPause() {
        super.onPause()
        presenter.editView = null
        markdownEditor?.hideKeyboard()
    }

    override fun onDestroy() {
        coroutineContext[Job]?.let {
            cancel()
        }
        super.onDestroy()
    }

    override fun onSelected() {
        markdownEditor?.showKeyboard()
    }

    override fun onDeselected() {
        markdownEditor?.hideKeyboard()
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
