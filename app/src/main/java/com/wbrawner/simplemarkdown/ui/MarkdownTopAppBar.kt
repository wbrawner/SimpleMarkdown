package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.wbrawner.simplemarkdown.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownTopAppBar(
    title: String,
    goBack: () -> Unit,
    backAsUp: Boolean = true,
    drawerState: DrawerState? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        colors = topAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surface),
        navigationIcon = {
            val (icon, contentDescription, onClick) = remember {
                if (backAsUp) {
                    Triple(Icons.AutoMirrored.Filled.ArrowBack, context.getString(R.string.action_back), goBack)
                } else {
                    Triple(
                        Icons.Default.Menu, context.getString(R.string.action_menu)
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
        scrollBehavior = scrollBehavior
    )
}