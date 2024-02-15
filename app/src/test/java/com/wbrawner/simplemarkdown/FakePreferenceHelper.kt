package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.utility.Preference
import com.wbrawner.simplemarkdown.utility.PreferenceHelper
import kotlinx.coroutines.flow.StateFlow

class FakePreferenceHelper: PreferenceHelper {
    val preferences = mutableMapOf<Preference, Any?>()
    override fun get(preference: Preference): Any? = preferences[preference]?: preference.default

    override fun set(preference: Preference, value: Any?) {
        preferences[preference] = value
    }

    override fun <T> observe(preference: Preference): StateFlow<T> {
        TODO("Not yet implemented")
    }
}