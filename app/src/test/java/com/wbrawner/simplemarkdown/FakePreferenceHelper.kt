package com.wbrawner.simplemarkdown

import com.wbrawner.simplemarkdown.utility.Preference
import com.wbrawner.simplemarkdown.utility.PreferenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePreferenceHelper: PreferenceHelper {
    val preferences = mutableMapOf<Preference<*>, Any?>()
    private val preferenceFlows = mutableMapOf<Preference<*>, MutableStateFlow<Any?>>()

    private fun <T> preferenceFlow(preference: Preference<T>) =
        preferenceFlows.getOrPut(preference) {
        MutableStateFlow(preference.default)
    }

    override fun <T> get(preference: Preference<T>): T =
        (preferences[preference] ?: preference.default).also {
            preferenceFlow(preference)
        } as T

    override fun <T> set(preference: Preference<T>, value: T) {
        preferenceFlow(preference).value = value
        preferences[preference] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> observe(preference: Preference<T>): StateFlow<T> =
        preferenceFlow(preference) as StateFlow<T>
}