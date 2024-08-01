package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.wbrawner.simplemarkdown.utility.readAssetToString

@OptIn(ExperimentalMaterial3Api::class)
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
                goBack = { navController.popBackStack() },
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        val (markdown, setMarkdown) = remember { mutableStateOf("") }
        LaunchedEffect(file) {
            setMarkdown(context.assets.readAssetToString(file))
        }
        MarkdownText(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            markdown = markdown
        )
    }
}
