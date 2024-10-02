package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.utility.Preference
import com.wbrawner.simplemarkdown.utility.PreferenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePreferenceHelper: PreferenceHelper {
    val preferences = mutableMapOf<Preference, Any?>()
    private val preferenceFlows = mutableMapOf<Preference, MutableStateFlow<Any?>>()

    private fun preferenceFlow(preference: Preference) = preferenceFlows.getOrPut(preference) {
        MutableStateFlow(preference.default)
    }

    override fun get(preference: Preference): Any? =
        (preferences[preference] ?: preference.default).also {
            preferenceFlow(preference)
        }

    override fun set(preference: Preference, value: Any?) {
        preferenceFlow(preference).value = value
        preferences[preference] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> observe(preference: Preference): StateFlow<T> =
        preferenceFlow(preference) as StateFlow<T>
}