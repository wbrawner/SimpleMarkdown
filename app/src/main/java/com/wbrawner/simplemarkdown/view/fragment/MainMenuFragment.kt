package com.wbrawner.simplemarkdown.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.ErrorHandler
import com.wbrawner.simplemarkdown.utility.readAssetToString
import com.wbrawner.simplemarkdown.utility.toHtml
import com.wbrawner.simplemarkdown.view.activity.MainActivity
import com.wbrawner.simplemarkdown.view.activity.MarkdownInfoActivity
import com.wbrawner.simplemarkdown.view.activity.SettingsActivity
import com.wbrawner.simplemarkdown.view.activity.SupportActivity
import kotlinx.android.synthetic.main.fragment_menu_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainMenuFragment : BottomSheetDialogFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    lateinit var errorHandler: ErrorHandler

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_menu_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainMenuNavigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_help -> showInfoActivity(context, R.id.action_help)
                R.id.action_settings -> {
                    val settingsIntent = Intent(context, SettingsActivity::class.java)
                    startActivityForResult(settingsIntent, MainActivity.REQUEST_DARK_MODE)
                }
                R.id.action_libraries -> showInfoActivity(context, R.id.action_libraries)
                R.id.action_privacy -> showInfoActivity(context, R.id.action_privacy)
                R.id.action_support -> Intent(context, SupportActivity::class.java)
                        .apply {
                            startActivity(this)
                            dialog?.dismiss()
                        }
            }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MainActivity.REQUEST_DARK_MODE) {
            activity?.recreate()
            dialog?.dismiss()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showInfoActivity(context: Context?, action: Int) {
        val infoIntent = Intent(context, MarkdownInfoActivity::class.java)
        var fileName = ""
        var title = ""
        when (action) {
            R.id.action_help -> {
                fileName = "Cheatsheet.md"
                title = getString(R.string.action_help)
            }
            R.id.action_libraries -> {
                fileName = "Libraries.md"
                title = getString(R.string.action_libraries)
            }
            R.id.action_privacy -> {
                fileName = "Privacy Policy.md"
                title = getString(R.string.action_privacy)
            }
        }
        infoIntent.putExtra("title", title)
        launch {
            // TODO: Refactor this to have the info activity load the markdown instead of doing
            //  it here
            try {
                val html = context?.assets?.readAssetToString(fileName)
                        ?.toHtml()
                        ?: throw RuntimeException("Unable to open stream to $fileName")
                infoIntent.putExtra("html", html)
                startActivity(infoIntent, null)
            } catch (e: Exception) {
                errorHandler.reportException(e)
                Toast.makeText(context, R.string.file_load_error, Toast.LENGTH_SHORT).show()
            }
            dialog?.dismiss()
        }
    }
}