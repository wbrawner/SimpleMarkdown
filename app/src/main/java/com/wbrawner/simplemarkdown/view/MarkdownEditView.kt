package com.wbrawner.simplemarkdown.view

interface MarkdownEditView {
    var markdown: String
    fun setTitle(title: String)

    fun onFileSaved(success: Boolean)

    fun onFileLoaded(success: Boolean)
}
