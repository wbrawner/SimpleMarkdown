//package com.wbrawner.simplemarkdown.view.fragment
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.MenuItem
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.ui.setupWithNavController
//import com.wbrawner.plausible.android.Plausible
//import com.wbrawner.simplemarkdown.R
//import com.wbrawner.simplemarkdown.databinding.FragmentSettingsBinding
//
//class SettingsContainerFragment : Fragment() {
//    private var _binding: FragmentSettingsBinding? = null
//    private val binding: FragmentSettingsBinding
//        get() = _binding!!
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        binding.toolbar.setupWithNavController(findNavController())
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean = findNavController().navigateUp()
//
//    override fun onStart() {
//        super.onStart()
//        Plausible.pageView("Settings")
//    }
//}
