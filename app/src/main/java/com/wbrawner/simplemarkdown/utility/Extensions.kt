package com.wbrawner.simplemarkdown.utility

import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.commonsware.cwac.anddown.AndDown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Reader

fun View.showKeyboard() =
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

fun View.hideKeyboard() =
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(windowToken, 0)

suspend fun AssetManager.readAssetToString(asset: String): String? {
    return withContext(Dispatchers.IO) {
        open(asset).reader().use(Reader::readText)
    }
}

const val HOEDOWN_FLAGS = AndDown.HOEDOWN_EXT_STRIKETHROUGH or AndDown.HOEDOWN_EXT_TABLES or
        AndDown.HOEDOWN_EXT_UNDERLINE or AndDown.HOEDOWN_EXT_SUPERSCRIPT or
        AndDown.HOEDOWN_EXT_FENCED_CODE

suspend fun String.toHtml(): String {
    return withContext(Dispatchers.IO) {
        AndDown().markdownToHtml(this@toHtml, HOEDOWN_FLAGS, 0)
    }
}

suspend fun Uri.getName(context: Context): String {
    var fileName: String? = null
    try {
        if ("content" == scheme) {
            withContext(Dispatchers.IO) {
                context.contentResolver.query(
                        this@getName,
                        null,
                        null,
                        null,
                        null
                )?.use {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    it.moveToFirst()
                    fileName = it.getString(nameIndex)
                }
            }
        } else if ("file" == scheme) {
            fileName = lastPathSegment
        }
    } catch (ignored: Exception) {
        ignored.printStackTrace()
    }
    return fileName ?: "Untitled.md"
}
