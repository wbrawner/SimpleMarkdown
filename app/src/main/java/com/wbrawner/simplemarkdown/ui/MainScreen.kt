package com.wbrawner.simplemarkdown.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.wbrawner.simplemarkdown.MarkdownViewModel
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.Route
import com.wbrawner.simplemarkdown.model.Readability
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URI

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MarkdownViewModel,
    enableAutosave: Boolean,
    enableReadability: Boolean,
    darkMode: String,
) {
    var lockSwiping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val fileName by viewModel.fileName.collectAsState()
    val openFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            coroutineScope.launch {
                viewModel.load(it.toString())
            }
        }
    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/*")) {
            it?.let {
                coroutineScope.launch {
                    viewModel.save(URI.create(it.toString()))
                }
            }
        }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var promptEffect by remember { mutableStateOf<MarkdownViewModel.Effect.Prompt?>(null) }
    var clearText by remember { mutableStateOf(0) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MarkdownViewModel.Effect.OpenSaveDialog -> saveFileLauncher.launch(fileName)
                is MarkdownViewModel.Effect.Error -> errorMessage = effect.text
                is MarkdownViewModel.Effect.Prompt -> promptEffect = effect
                is MarkdownViewModel.Effect.ClearText -> clearText++
            }
        }
    }
    LaunchedEffect(enableAutosave) {
        if (!enableAutosave) return@LaunchedEffect
        while (isActive) {
            delay(30_000)
            viewModel.autosave()
        }
    }
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            },
            text = { Text(message) }
        )
    }
    promptEffect?.let { prompt ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                TextButton(onClick = {
                    prompt.confirm()
                    promptEffect = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    prompt.cancel()
                    promptEffect = null
                }) {
                    Text("No")
                }
            },
            text = { Text(prompt.text) }
        )
    }
    MarkdownNavigationDrawer(navigate = { navController.navigate(it.path) }) { drawerState ->
        Scaffold(topBar = {
            val context = LocalContext.current
            MarkdownTopAppBar(title = fileName,
                backAsUp = false,
                navController = navController,
                drawerState = drawerState,
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, viewModel.markdown.value)
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
                                coroutineScope.launch {
                                    viewModel.reset("Untitled.md")
                                }
                            })
                            DropdownMenuItem(text = { Text("Open") }, onClick = {
                                menuExpanded = false
                                openFileLauncher.launch(arrayOf("text/*"))
                            })
                            DropdownMenuItem(text = { Text("Save") }, onClick = {
                                menuExpanded = false
                                coroutineScope.launch {
                                    viewModel.save()
                                }
                            })
                            DropdownMenuItem(text = { Text("Save as…") },
                                onClick = {
                                    menuExpanded = false
                                    saveFileLauncher.launch(fileName)
                                })
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
                })
        }) { paddingValues ->
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
                    val markdown by viewModel.markdown.collectAsState()
                    var textFieldValue by remember(clearText) {
                        val annotatedMarkdown = if (enableReadability) {
                            markdown.annotateReadability()
                        } else {
                            AnnotatedString(markdown)
                        }
                        mutableStateOf(TextFieldValue(annotatedMarkdown))
                    }
                    if (page == 0) {
                        TextField(
                            modifier = Modifier
                                .fillMaxSize(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            value = textFieldValue,
                            onValueChange = {
                                textFieldValue = if (enableReadability) {
                                    it.copy(annotatedString = it.text.annotateReadability())
                                } else {
                                    it
                                }
                                coroutineScope.launch {
                                    viewModel.updateMarkdown(it.text)
                                }
                            },
                            placeholder = {
                                Text("Markdown here…")
                            },
                            textStyle = TextStyle.Default.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                    } else {
                        MarkdownPreview(modifier = Modifier.fillMaxSize(), markdown, darkMode)
                    }
                }
            }
        }
    }
}

private fun String.annotateReadability(): AnnotatedString {
    val readability = Readability(this)
    val annotated = AnnotatedString.Builder(this)
    for (sentence in readability.sentences()) {
        var color = Color.Transparent
        if (sentence.syllableCount() > 25) color = Color(229, 232, 42, 100)
        if (sentence.syllableCount() > 35) color = Color(193, 66, 66, 100)
        annotated.addStyle(SpanStyle(background = color), sentence.start(), sentence.end())
    }
    return annotated.toAnnotatedString()
}

@Composable
fun MarkdownNavigationDrawer(
    navigate: (Route) -> Unit, content: @Composable (drawerState: DrawerState) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    DismissibleNavigationDrawer(drawerState = drawerState, drawerContent = {
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
