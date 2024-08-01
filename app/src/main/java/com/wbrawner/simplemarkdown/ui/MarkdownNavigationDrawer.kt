package com.wbrawner.simplemarkdown.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.Route
import kotlinx.coroutines.launch

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
                        text = stringResource(R.string.app_name),
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
                        label = { Text(stringResource(route.title)) },
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