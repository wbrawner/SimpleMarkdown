package com.wbrawner.simplemarkdown.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wbrawner.simplemarkdown.ui.theme.SimpleMarkdownTheme
import com.wbrawner.simplemarkdown.utility.Preference
import com.wbrawner.simplemarkdown.utility.PreferenceHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, preferenceHelper: PreferenceHelper) {
    Scaffold(topBar = {
        MarkdownTopAppBar(title = "Settings", goBack = { navController.popBackStack() })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            BooleanPreference(
                title = "Autosave",
                enabledDescription = "Files will be saved automatically",
                disabledDescription = "Files will not be saved automatically",
                preference = Preference.AUTOSAVE_ENABLED,
                preferenceHelper = preferenceHelper
            )
            ListPreference(
                title = "Dark mode",
                options = listOf("auto", "dark", "light"),
                preference = Preference.DARK_MODE,
                preferenceHelper = preferenceHelper
            )
            BooleanPreference(
                title = "Send crash reports",
                enabledDescription = "Error reports will be sent",
                disabledDescription = "Error reports will not be sent",
                preference = Preference.ERROR_REPORTS_ENABLED,
                preferenceHelper = preferenceHelper
            )
            BooleanPreference(
                title = "Send analytics",
                enabledDescription = "Analytics events will be sent",
                disabledDescription = "Analytics events will not be sent",
                preference = Preference.ANALYTICS_ENABLED,
                preferenceHelper = preferenceHelper
            )
            BooleanPreference(
                title = "Readability highlighting",
                enabledDescription = "Readability highlighting is on",
                disabledDescription = "Readability highlighting is off",
                preference = Preference.READABILITY_ENABLED,
                preferenceHelper = preferenceHelper
            )
        }
    }
}

@Composable
fun BooleanPreference(
    title: String,
    enabledDescription: String,
    disabledDescription: String,
    preference: Preference,
    preferenceHelper: PreferenceHelper
) {
    var enabled by remember {
        mutableStateOf(preferenceHelper[preference] as Boolean)
    }
    BooleanPreference(title = title,
        enabledDescription = enabledDescription,
        disabledDescription = disabledDescription,
        enabled = enabled,
        setEnabled = {
            enabled = it
            preferenceHelper[preference] = it
        })
}

@Composable
fun BooleanPreference(
    title: String,
    enabledDescription: String,
    disabledDescription: String,
    enabled: Boolean,
    setEnabled: (Boolean) -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            setEnabled(!enabled)
        }
        .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (enabled) enabledDescription else disabledDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = enabled, onCheckedChange = setEnabled)
    }
}

@Composable
fun ListPreference(
    title: String,
    options: List<String>,
    preference: Preference,
    preferenceHelper: PreferenceHelper
) {
    var selected by remember {
        mutableStateOf(preferenceHelper[preference] as String)
    }

    ListPreference(title = title, options = options, selected = selected, setSelected = {
        selected = it
        preferenceHelper[preference] = it
    })
}

@Composable
fun ListPreference(
    title: String, options: List<String>, selected: String, setSelected: (String) -> Unit
) {
    var dialogShowing by remember { mutableStateOf(false) }
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            dialogShowing = true
        }
        .padding(16.dp), verticalArrangement = Arrangement.Center) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = selected,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (dialogShowing) {
        AlertDialog(
            title = {
                Text(title)
            },
            onDismissRequest = { dialogShowing = false },
            confirmButton = {
                TextButton(onClick = { dialogShowing = false }) {
                    Text("Cancel")
                }
            },
            text = {
                Column {
                    options.forEach { option ->
                        val onClick = {
                            setSelected(option)
                            dialogShowing = false
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onClick),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = option == selected, onClick = onClick)
                            Text(option)
                        }
                    }
                }
            }
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun BooleanPreference_Preview() {
    val (enabled, setEnabled) = remember { mutableStateOf(true) }
    SimpleMarkdownTheme {
        Surface {
            BooleanPreference(
                "Autosave",
                "Files will be saved automatically",
                "Files will not be saved automatically",
                enabled,
                setEnabled
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ListPreference_Preview() {
    val (selected, setSelected) = remember { mutableStateOf("Auto") }
    SimpleMarkdownTheme {
        Surface {
            ListPreference(
                "Dark mode", listOf("Light", "Dark", "Auto"), selected, setSelected
            )
        }
    }
}