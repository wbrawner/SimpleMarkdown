package com.wbrawner.simplemarkdown.view.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val viewModel: MarkdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intent?.data?.let {
            launch {
                viewModel.load(this@MainActivity, it)
            }
        }
    }

    override fun onBackPressed() {
        findNavController(R.id.content).navigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext[Job]?.let {
            cancel()
        }
    }
}
