package com.wbrawner.simplemarkdown.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wbrawner.simplemarkdown.AlertDialogModel
import com.wbrawner.simplemarkdown.EditorState
import com.wbrawner.simplemarkdown.MarkdownViewModel
import com.wbrawner.simplemarkdown.ParameterizedText
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.Route
import com.wbrawner.simplemarkdown.utility.activity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.URI
import kotlin.reflect.KProperty1

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MarkdownViewModel,
    enableWideLayout: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val fileName by viewModel.collectAsState(EditorState::fileName, "")
    val markdownTextFieldState by viewModel.collectAsState(
        EditorState::textFieldState,
        TextFieldState()
    )
    val dirty by viewModel.collectAsState(EditorState::dirty, false)
    val alert by viewModel.collectAsState(EditorState::alert, null)
    val saveCallback by viewModel.collectAsState(EditorState::saveCallback, null)
    val lockSwiping by viewModel.collectAsState(EditorState::lockSwiping, false)
    val enableReadability by viewModel.collectAsState(EditorState::enableReadability, false)
    val toast by viewModel.collectAsState(EditorState::toast, null)
    val exitApp by viewModel.collectAsState(EditorState::exitApp, false)
    val activity = LocalContext.current.activity
    LaunchedEffect(exitApp) {
        if (exitApp) {
            activity?.finish()
        }
    }
    BackHandler(onBack = viewModel::onBackPressed)

    MainScreen(
        dirty = dirty,
        fileName = fileName,
        markdownTextFieldState = markdownTextFieldState,
        lockSwiping = lockSwiping,
        toggleLockSwiping = viewModel::setLockSwiping,
        message = toast?.stringRes(),
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
        markdownUpdated = viewModel::markdownUpdated,
        enableWideLayout = enableWideLayout,
        enableReadability = enableReadability,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    fileName: String = "Untitled.md",
    dirty: Boolean = false,
    markdownTextFieldState: TextFieldState = TextFieldState(),
    lockSwiping: Boolean,
    toggleLockSwiping: (Boolean) -> Unit,
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
    markdownUpdated: () -> Unit = {},
    enableWideLayout: Boolean = false,
    enableReadability: Boolean = false,
) {
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

    LaunchedEffect(markdownTextFieldState) {
        snapshotFlow { markdownTextFieldState.text }
            .collectLatest { markdownUpdated() }
    }

    alert?.let {
        AlertDialog(
            onDismissRequest = dismissAlert,
            confirmButton = {
                TextButton(onClick = it.confirmButton.onClick) {
                    Text(stringResource(it.confirmButton.text.text))
                }
            },
            dismissButton = {
                it.dismissButton?.let { dismissButton ->
                    TextButton(onClick = dismissButton.onClick) {
                        Text(dismissButton.text.stringRes())
                    }
                }
            },
            text = { Text(it.text.stringRes()) }
        )
    }
    var backPressed by remember { mutableStateOf(false) }
    BackHandler(enabled = !backPressed) {
        backPressed = true
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    MarkdownNavigationDrawer(navigate) { drawerState ->
        Scaffold(
            modifier = Modifier.onKeyEvent { keyEvent ->
                if (
                    keyEvent.type != KeyEventType.KeyUp
                    || !keyEvent.isCtrlPressed
                    || keyEvent.isAltPressed
                    || keyEvent.isMetaPressed
                ) {
                    return@onKeyEvent false
                }

                when (keyEvent.key) {
                    Key.N -> {
                        if (!keyEvent.isShiftPressed) {
                            reset()
                            true
                        } else {
                            false
                        }
                    }

                    Key.O -> {
                        if (!keyEvent.isShiftPressed) {
                            openFileLauncher.launch(arrayOf("text/*"))
                            true
                        } else {
                            false
                        }
                    }

                    Key.S -> {
                        if (keyEvent.isShiftPressed) {
                            saveFileLauncher.launch(fileName)
                        } else {
                            saveFile(null)
                        }
                        true
                    }

                    else -> false
                }
            },
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
                            shareIntent.putExtra(
                                Intent.EXTRA_TEXT,
                                markdownTextFieldState.text.toString()
                            )
                            shareIntent.type = "text/plain"
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent, context.getString(R.string.share_file)
                                ), null
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.action_share)
                            )
                        }
                        Box {
                            var menuExpanded by remember { mutableStateOf(false) }
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    stringResource(R.string.action_editor_actions)
                                )
                            }
                            DropdownMenu(expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_new)) },
                                    onClick = {
                                        menuExpanded = false
                                        reset()
                                    })
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_open)) },
                                    onClick = {
                                        menuExpanded = false
                                        openFileLauncher.launch(arrayOf("text/*"))
                                    })
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_save)) },
                                    onClick = {
                                        menuExpanded = false
                                        saveFile(null)
                                    })
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_save_as)) },
                                    onClick = {
                                        menuExpanded = false
                                        saveFileLauncher.launch(fileName)
                                    })
                                if (!enableWideLayout) {
                                    DropdownMenuItem(text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(R.string.action_lock_swipe))
                                            Checkbox(
                                                checked = lockSwiping,
                                                onCheckedChange = toggleLockSwiping
                                            )
                                        }
                                    }, onClick = {
                                        toggleLockSwiping(!lockSwiping)
                                        menuExpanded = false
                                    })
                                }
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
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
                        textFieldState = markdownTextFieldState,
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
                        markdown = markdownTextFieldState.text.toString()
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabbedMarkdownEditor(
                        markdownTextFieldState = markdownTextFieldState,
                        lockSwiping = lockSwiping,
                        enableReadability = enableReadability,
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TabbedMarkdownEditor(
    markdownTextFieldState: TextFieldState,
    lockSwiping: Boolean,
    enableReadability: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    TabRow(selectedTabIndex = pagerState.currentPage) {
        Tab(text = { Text(stringResource(R.string.action_edit)) },
            selected = pagerState.currentPage == 0,
            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } })
        Tab(text = { Text(stringResource(R.string.action_preview)) },
            selected = pagerState.currentPage == 1,
            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } })
    }
    HorizontalPager(
        modifier = Modifier.fillMaxSize(1f), state = pagerState,
        beyondViewportPageCount = 1,
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
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                textFieldState = markdownTextFieldState,
                enableReadability = enableReadability
            )
        } else {
            MarkdownText(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                markdownTextFieldState.text.toString()
            )
        }
    }
}

@Composable
fun <P> MarkdownViewModel.collectAsState(prop: KProperty1<EditorState, P>, initial: P): State<P> =
    remember(prop) { state.map { prop.get(it) }.distinctUntilChanged() }.collectAsState(initial)

@Composable
fun ParameterizedText.stringRes() = stringResource(text, *params)