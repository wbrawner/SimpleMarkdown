package com.wbrawner.simplemarkdown.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.AnalyticsHelper
import com.wbrawner.simplemarkdown.utility.init
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsContainerFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setupWithNavController(findNavController())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = findNavController().navigateUp()

    override fun onStart() {
        super.onStart()
        AnalyticsHelper.init(requireContext()).trackPageView("Settings")
    }
}
