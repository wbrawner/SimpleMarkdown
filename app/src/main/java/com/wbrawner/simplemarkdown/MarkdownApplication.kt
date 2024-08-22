package com.wbrawner.simplemarkdown

import android.app.Application
import android.os.StrictMode
import com.wbrawner.simplemarkdown.core.ErrorReporterTree
import com.wbrawner.simplemarkdown.utility.AndroidFileHelper
import com.wbrawner.simplemarkdown.utility.AndroidPreferenceHelper
import com.wbrawner.simplemarkdown.utility.FileHelper
import com.wbrawner.simplemarkdown.utility.PersistentTree
import com.wbrawner.simplemarkdown.utility.PreferenceHelper
import com.wbrawner.simplemarkdown.utility.ReviewHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MarkdownApplication : Application() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

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
            coroutineScope.launch {
                try {
                    Timber.plant(PersistentTree.create(coroutineScope, File(getExternalFilesDir(null), "logs")))
                } catch (e: Exception) {
                    Timber.e(e, "Unable to create PersistentTree")
                }
            }
        }
        coroutineScope.launch {
            Timber.plant(ErrorReporterTree.create(this@MarkdownApplication))
        }
        super.onCreate()
        ReviewHelper.init(this)
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
