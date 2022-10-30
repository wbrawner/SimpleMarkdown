package com.wbrawner.simplemarkdown.view.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.wbrawner.plausible.android.Plausible
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.SupportLinkProvider
import kotlinx.android.synthetic.main.fragment_support.*

class SupportFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_support, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setupWithNavController(findNavController())
        githubButton.setOnClickListener {
            CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .build()
                    .launchUrl(view.context, Uri.parse("https://github" +
                            ".com/wbrawner/SimpleMarkdown"))
        }
        rateButton.setOnClickListener {
            val playStoreIntent = Intent(Intent.ACTION_VIEW)
                    .apply {
                        data = Uri.parse("market://details?id=${view.context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    }
            try {
                startActivity(playStoreIntent)
            } catch (ignored: ActivityNotFoundException) {
                playStoreIntent.data = Uri.parse("https://play.google.com/store/apps/details?id=${view.context.packageName}")
                startActivity(playStoreIntent)
            }
        }
        SupportLinkProvider(requireActivity()).supportLinks.observe(viewLifecycleOwner, Observer { links ->
            links.forEach {
                supportButtons.addView(it)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Plausible.pageView("Support")
    }

    //    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == android.R.id.home) {
//            findNavController().navigateUp()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }
}