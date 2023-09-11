package com.wbrawner.simplemarkdown.ui

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.view.activity.Route
import com.wbrawner.simplemarkdown.viewmodel.MarkdownViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController, viewModel: MarkdownViewModel) {
    var lockSwiping by remember { mutableStateOf(false) }
    MarkdownNavigationDrawer(navigate = { navController.navigate(it.path) }) { drawerState ->
        Scaffold(topBar = {
            val fileName by viewModel.fileName.collectAsState()
            val context = LocalContext.current
            MarkdownTopAppBar(title = fileName,
                backAsUp = false,
                navController = navController,
                drawerState = drawerState,
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, viewModel.markdownUpdates.value)
                        shareIntent.type = "text/plain"
                        startActivity(
                            context, Intent.createChooser(
                                shareIntent, context.getString(R.string.share_file)
                            ), null
                        )
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                    Box {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, "Editor Actions")
                        }
                        DropdownMenu(expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("New") }, onClick = {
                                menuExpanded = false
                            })
                            DropdownMenuItem(text = { Text("Open") }, onClick = {
                                menuExpanded = false
                            })
                            DropdownMenuItem(text = { Text("Save") }, onClick = {
                                menuExpanded = false
                            })
                            DropdownMenuItem(text = { Text("Save as…") },
                                onClick = {
                                    menuExpanded = false
                                })
                            DropdownMenuItem(text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Lock Swiping")
                                    Checkbox(checked = lockSwiping, onCheckedChange = { lockSwiping = !lockSwiping })
                                }
                            }, onClick = {
                                lockSwiping = !lockSwiping
                                menuExpanded = false
                            })
                        }
                    }
                })
        }) { paddingValues ->
            val coroutineScope = rememberCoroutineScope()
            val pagerState = rememberPagerState { 2 }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    Tab(text = { Text("Edit") },
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } })
                    Tab(text = { Text("Preview") },
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } })
                }
                HorizontalPager(
                    modifier = Modifier.weight(1f), state = pagerState,
                    userScrollEnabled = !lockSwiping
                ) { page ->
                    val markdown by viewModel.markdownUpdates.collectAsState()
                    if (page == 0) {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            value = markdown,
                            onValueChange = { viewModel.updateMarkdown(it) },
                            textStyle = TextStyle.Default.copy(fontFamily = FontFamily.Monospace)
                        )
                    } else {
                        MarkdownPreview(modifier = Modifier.fillMaxSize(), markdown)
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownNavigationDrawer(
    navigate: (Route) -> Unit, content: @Composable (drawerState: DrawerState) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    DismissibleNavigationDrawer(drawerState = drawerState, drawerContent = {
        DismissibleDrawerSheet {
            Route.entries.forEach { route ->
                if (route == Route.EDITOR) {
                    return@forEach
                }
                NavigationDrawerItem(icon = {
                    Icon(imageVector = route.icon, contentDescription = null)
                },
                    label = { Text(route.title) },
                    selected = false,
                    onClick = { navigate(route) })
            }
        }
    }) {
        content(drawerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownTopAppBar(
    title: String,
    navController: NavController,
    backAsUp: Boolean = true,
    drawerState: DrawerState? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    TopAppBar(title = {
        Text(title)
    }, navigationIcon = {
        val (icon, contentDescription, onClick) = remember {
            if (backAsUp) {
                Triple(Icons.Default.ArrowBack, "Go Back") { navController.popBackStack() }
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
    }, actions = actions ?: {})
}
