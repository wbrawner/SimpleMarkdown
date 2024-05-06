package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownTopAppBar(
    title: String,
    goBack: () -> Unit,
    backAsUp: Boolean = true,
    drawerState: DrawerState? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    TopAppBar(
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            val (icon, contentDescription, onClick) = remember {
                if (backAsUp) {
                    Triple(Icons.AutoMirrored.Filled.ArrowBack, "Go Back", goBack)
                } else {
                    Triple(
                        Icons.Default.Menu, "Main Menu"
                    ) {
                        coroutineScope.launch {
                            if (drawerState?.isOpen == true) {
                                drawerState.close()
                            } else {
                                drawerState?.open()
                            }
                        }
                    }
                }
            }
            IconButton(onClick = { onClick() }) {
                Icon(imageVector = icon, contentDescription = contentDescription)
            }
        },
        actions = actions ?: {},
    )
}