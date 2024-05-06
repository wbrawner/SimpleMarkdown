package com.wbrawner.simplemarkdown.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.wbrawner.simplemarkdown.AlertDialogModel
import com.wbrawner.simplemarkdown.EditorState
import com.wbrawner.simplemarkdown.MarkdownViewModel
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URI
import kotlin.reflect.KProperty1

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MarkdownViewModel,
    enableWideLayout: Boolean,
    enableAutosave: Boolean,
    enableReadability: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val fileName by viewModel.collectAsState(EditorState::fileName, "")
    val initialMarkdown by viewModel.collectAsState(EditorState::markdown, "")
    val reloadToggle by viewModel.collectAsState(EditorState::reloadToggle, 0)
    val markdown by viewModel.collectAsState(EditorState::markdown, "")
    val dirty by viewModel.collectAsState(EditorState::dirty, false)
    val alert by viewModel.collectAsState(EditorState::alert, null)
    val saveCallback by viewModel.collectAsState(EditorState::saveCallback, null)
    LaunchedEffect(enableAutosave) {
        if (!enableAutosave) return@LaunchedEffect
        while (isActive) {
            delay(500)
            viewModel.autosave()
        }
    }
    val toast by viewModel.collectAsState(EditorState::toast, null)
    MainScreen(
        dirty = dirty,
        fileName = fileName,
        reloadToggle = reloadToggle,
        initialMarkdown = initialMarkdown,
        markdown = markdown,
        setMarkdown = viewModel::updateMarkdown,
        message = toast,
        dismissMessage = viewModel::dismissToast,
        alert = alert,
        dismissAlert = viewModel::dismissAlert,
        navigate = {
            navController.navigate(it.path)
        },
        navigateBack = { navController.popBackStack() },
        loadFile = {
            coroutineScope.launch {
                viewModel.load(it.toString())
            }
        },
        saveFile = {
            coroutineScope.launch {
                viewModel.save(it)
            }
        },
        saveCallback = saveCallback,
        reset = {
            viewModel.reset("Untitled.md")
        },
        enableWideLayout = enableWideLayout,
        enableReadability = enableReadability,
    )
}

@Composable
private fun MainScreen(
    fileName: String = "Untitled.md",
    dirty: Boolean = false,
    reloadToggle: Int = 0,
    initialMarkdown: String = "",
    markdown: String = "",
    setMarkdown: (String) -> Unit = {},
    message: String? = null,
    dismissMessage: () -> Unit = {},
    alert: AlertDialogModel? = null,
    dismissAlert: () -> Unit = {},
    navigate: (Route) -> Unit = {},
    navigateBack: () -> Unit = {},
    loadFile: (Uri?) -> Unit = {},
    saveFile: (URI?) -> Unit = {},
    saveCallback: (() -> Unit)? = null,
    reset: () -> Unit = {},
    enableWideLayout: Boolean = false,
    enableReadability: Boolean = false
) {
    var lockSwiping by remember { mutableStateOf(true) }
    val openFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            loadFile(it)
        }
    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/*")) {
            it?.let { uri -> saveFile(URI.create(uri.toString())) }
        }
    saveCallback?.let { callback ->
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/*")) {
                it?.let { uri -> saveFile(URI.create(uri.toString())) }
                callback()
            }
        LaunchedEffect(callback) {
            launcher.launch(fileName)
        }
    }

    val snackBarState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackBarState.showSnackbar(it)
            dismissMessage()
        }
    }

    alert?.let {
        AlertDialog(
            onDismissRequest = dismissAlert,
            confirmButton = {
                TextButton(onClick = it.confirmButton.onClick) {
                    Text(it.confirmButton.text)
                }
            },
            dismissButton = {
                it.dismissButton?.let { dismissButton ->
                    TextButton(onClick = dismissButton.onClick) {
                        Text(dismissButton.text)
                    }
                }
            },
            text = { Text(it.text) }
        )
    }

    MarkdownNavigationDrawer(navigate) { drawerState ->
        Scaffold(
            topBar = {
                val context = LocalContext.current
                MarkdownTopAppBar(
                    title = if (dirty) "$fileName*" else fileName,
                    backAsUp = false,
                    goBack = navigateBack,
                    drawerState = drawerState,
                    actions = {
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.putExtra(Intent.EXTRA_TEXT, markdown)
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
                                    reset()
                                })
                                DropdownMenuItem(text = { Text("Open") }, onClick = {
                                    menuExpanded = false
                                    openFileLauncher.launch(arrayOf("text/*"))
                                })
                                DropdownMenuItem(text = { Text("Save") }, onClick = {
                                    menuExpanded = false
                                    saveFile(null)
                                })
                                DropdownMenuItem(text = { Text("Save as…") },
                                    onClick = {
                                        menuExpanded = false
                                        saveFileLauncher.launch(fileName)
                                    })
                                if (!enableWideLayout) {
                                    DropdownMenuItem(text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Lock Swiping")
                                            Checkbox(
                                                checked = lockSwiping,
                                                onCheckedChange = { lockSwiping = !lockSwiping })
                                        }
                                    }, onClick = {
                                        lockSwiping = !lockSwiping
                                        menuExpanded = false
                                    })
                                }
                            }
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(
                    modifier = Modifier.imePadding(),
                    hostState = snackBarState
                ) {
                    Snackbar(it)
                }
            }
        ) { paddingValues ->
            if (enableWideLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    MarkdownTextField(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        reload = reloadToggle,
                        markdown = markdown,
                        setMarkdown = setMarkdown,
                        enableReadability = enableReadability
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(color = MaterialTheme.colorScheme.primary)
                    )
                    MarkdownText(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        markdown = markdown
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabbedMarkdownEditor(
                        initialMarkdown = initialMarkdown,
                        markdown = markdown,
                        setMarkdown = setMarkdown,
                        lockSwiping = lockSwiping,
                        enableReadability = enableReadability,
                        reloadToggle = reloadToggle
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TabbedMarkdownEditor(
    initialMarkdown: String,
    markdown: String,
    setMarkdown: (String) -> Unit,
    lockSwiping: Boolean,
    enableReadability: Boolean,
    reloadToggle: Int
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    TabRow(selectedTabIndex = pagerState.currentPage) {
        Tab(text = { Text("Edit") },
            selected = pagerState.currentPage == 0,
            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } })
        Tab(text = { Text("Preview") },
            selected = pagerState.currentPage == 1,
            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } })
    }
    HorizontalPager(
        modifier = Modifier.fillMaxSize(1f), state = pagerState,
        beyondBoundsPageCount = 1,
        userScrollEnabled = !lockSwiping
    ) { page ->
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(page) {
            when (page) {
                0 -> keyboardController?.show()
                else -> keyboardController?.hide()
            }
        }
        if (page == 0) {
            MarkdownTextField(
                modifier = Modifier.fillMaxSize(),
                markdown = initialMarkdown,
                setMarkdown = setMarkdown,
                enableReadability = enableReadability,
                reload = reloadToggle
            )
        } else {
            MarkdownText(modifier = Modifier.fillMaxSize(), markdown)
        }
    }
}

@Composable
fun MarkdownNavigationDrawer(
    navigate: (Route) -> Unit, content: @Composable (drawerState: DrawerState) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    DismissibleNavigationDrawer(
        gesturesEnabled = false,
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(96.dp),
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Simple Markdown",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Route.entries.forEach { route ->
                    if (route == Route.EDITOR) {
                        return@forEach
                    }
                    NavigationDrawerItem(
                        icon = {
                            Icon(imageVector = route.icon, contentDescription = null)
                        },
                        label = { Text(route.title) },
                        selected = false,
                        onClick = {
                            navigate(route)
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }
                    )
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

@Composable
fun <P> MarkdownViewModel.collectAsState(prop: KProperty1<EditorState, P>, initial: P): State<P> =
    state.map { prop.get(it) }
        .collectAsState(initial)