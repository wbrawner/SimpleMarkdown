package com.wbrawner.simplemarkdown.utility

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.google.android.material.button.MaterialButton

class SupportLinkProvider(@Suppress("unused") private val activity: Activity) {
    val supportLinks = MutableLiveData<List<MaterialButton>>()
}