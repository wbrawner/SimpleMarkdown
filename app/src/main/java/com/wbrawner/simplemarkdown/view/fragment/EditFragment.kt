package com.wbrawner.simplemarkdown.view.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.model.Readability
import com.wbrawner.simplemarkdown.utility.hideKeyboard
import com.wbrawner.simplemarkdown.utility.showKeyboard
import com.wbrawner.simplemarkdown.view.ViewPagerPage
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class EditFragment : Fragment(), ViewPagerPage, CoroutineScope {
    private var markdownEditor: EditText? = null
    private var markdownEditorScroller: ScrollView? = null
    private val viewModel: MarkdownViewModel by activityViewModels()
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private var readabilityWatcher: TextWatcher? = null

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
                    viewModel.updateMarkdown(searchText)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

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
        viewModel.originalMarkdown.observe(viewLifecycleOwner, Observer {
            markdownEditor?.setText(it)
        })
        launch {
            val enableReadability = withContext(Dispatchers.IO) {
                context?.let {
                    PreferenceManager.getDefaultSharedPreferences(it)
                            .getBoolean(getString(R.string.readability_enabled), false)
                }?: false
            }
            if (enableReadability) {
                if (readabilityWatcher == null) {
                    readabilityWatcher = ReadabilityTextWatcher()
                }
                markdownEditor?.addTextChangedListener(readabilityWatcher)
            } else {
                readabilityWatcher?.let {
                    markdownEditor?.removeTextChangedListener(it)
                }
                readabilityWatcher = null
            }

        }
    }

    override fun onDestroyView() {
        coroutineContext[Job]?.let {
            cancel()
        }
        super.onDestroyView()
    }

    override fun onSelected() {
        markdownEditor?.showKeyboard()
    }

    override fun onDeselected() {
        markdownEditor?.hideKeyboard()
    }

    inner class ReadabilityTextWatcher : TextWatcher {
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
                if (searchFor.isEmpty()) return@launch
                if (previousValue == searchFor) return@launch
                val readability = Readability(searchFor)
                val span = SpannableString(searchFor)
                for (sentence in readability.sentences()) {
                    var color = Color.TRANSPARENT
                    if (sentence.syllableCount() > 25) color = Color.argb(100, 229, 232, 42)
                    if (sentence.syllableCount() > 35) color = Color.argb(100, 193, 66, 66)
                    Log.d("SimpleMarkdown", "Sentence start: ${sentence.start()} end: ${sentence.end()}")
                    span.setSpan(BackgroundColorSpan(color), sentence.start(), sentence.end(), 0)
                }
                markdownEditor?.setTextKeepState(span, TextView.BufferType.SPANNABLE)
                previousValue = searchFor
                val timeTakenMs = System.currentTimeMillis() - start
                Log.d("SimpleMarkdown", "Handled markdown in $timeTakenMs ms")
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
}
