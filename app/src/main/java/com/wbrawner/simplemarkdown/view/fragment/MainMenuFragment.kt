package com.wbrawner.simplemarkdown.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.view.activity.MainActivity
import com.wbrawner.simplemarkdown.view.activity.MarkdownInfoActivity
import com.wbrawner.simplemarkdown.view.activity.MarkdownInfoActivity.Companion.EXTRA_FILE
import com.wbrawner.simplemarkdown.view.activity.MarkdownInfoActivity.Companion.EXTRA_TITLE
import com.wbrawner.simplemarkdown.view.activity.SettingsActivity
import com.wbrawner.simplemarkdown.view.activity.SupportActivity
import kotlinx.android.synthetic.main.fragment_menu_main.*

class MainMenuFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_menu_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainMenuNavigationView.setNavigationItemSelectedListener { menuItem ->
            val (intentClass, fileName, title) = when (menuItem.itemId) {
                R.id.action_help -> Triple(
                        MarkdownInfoActivity::class.java,
                        "Cheatsheet.md",
                        R.string.action_help
                )
                R.id.action_settings -> Triple(
                        SettingsActivity::class.java,
                        null,
                        null
                )
                R.id.action_libraries -> Triple(
                        MarkdownInfoActivity::class.java,
                        "Libraries.md",
                        R.string.action_libraries
                )
                R.id.action_privacy -> Triple(
                        MarkdownInfoActivity::class.java,
                        "Privacy Policy.md",
                        R.string.action_privacy
                )
                R.id.action_support -> Triple(
                        SupportActivity::class.java,
                        null,
                        null
                )
                else -> throw IllegalStateException("This shouldn't happen")
            }
            val intent = Intent(context, intentClass)
            fileName?.let {
                intent.putExtra(EXTRA_FILE, it)
            }
            title?.let {
                intent.putExtra(EXTRA_TITLE, getString(it))
            }
            if (intentClass == SettingsActivity::class.java) {
                startActivityForResult(intent, MainActivity.REQUEST_DARK_MODE)
            } else {
                startActivity(intent)
                dialog?.dismiss()
            }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MainActivity.REQUEST_DARK_MODE) {
            activity?.recreate()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        dialog?.dismiss()
    }
}