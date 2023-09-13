package com.wbrawner.simplemarkdown

import android.app.Application
import android.os.StrictMode
import com.wbrawner.plausible.android.Plausible
import com.wbrawner.simplemarkdown.utility.AndroidFileHelper
import com.wbrawner.simplemarkdown.utility.AndroidPreferenceHelper
import com.wbrawner.simplemarkdown.utility.FileHelper
import com.wbrawner.simplemarkdown.utility.PersistentTree
import com.wbrawner.simplemarkdown.utility.PreferenceHelper
import com.wbrawner.simplemarkdown.utility.ReviewHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MarkdownApplication : Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            Timber.plant(Timber.DebugTree())
            GlobalScope.launch {
                try {
                    Timber.plant(PersistentTree.create(File(getExternalFilesDir(null), "logs")))
                } catch (e: Exception) {
                    Timber.e(e, "Unable to create PersistentTree")
                }
            }
        }
        super.onCreate()
        ReviewHelper.init(this)
        Plausible.enable(!BuildConfig.DEBUG)
        Plausible.event(
            "build",
            url = "/",
            props = mapOf(
            "Flavor" to BuildConfig.FLAVOR,
            "App Version" to BuildConfig.VERSION_NAME
        ))
        fileHelper = AndroidFileHelper(this)
        preferenceHelper = AndroidPreferenceHelper(this)
    }

    companion object {
        lateinit var fileHelper: FileHelper
            private set
        lateinit var preferenceHelper: PreferenceHelper
            private set
    }
}
