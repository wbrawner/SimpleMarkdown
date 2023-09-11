package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.wbrawner.simplemarkdown.utility.readAssetToString
import com.wbrawner.simplemarkdown.utility.toHtml
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel

@Composable
fun MarkdownInfoScreen(
    title: String,
    file: String,
    navController: NavController,
) {
    Scaffold(
        topBar = {
            MarkdownTopAppBar(
                title = title,
                navController = navController,
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        var markdown by remember { mutableStateOf("") }
        LaunchedEffect(file) {
            markdown = context.assets.readAssetToString(file)?.toHtml()?: "Failed to load $file"
        }
        MarkdownPreview(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            markdown = markdown
        )
    }
}
