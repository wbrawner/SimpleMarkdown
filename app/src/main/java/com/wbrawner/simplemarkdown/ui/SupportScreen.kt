package com.wbrawner.simplemarkdown.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.utility.SupportLinks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavController) {
    Scaffold(topBar = {
        MarkdownTopAppBar(title = stringResource(R.string.support_title), goBack = { navController.popBackStack() })
    }) { paddingValues ->
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(100.dp),
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(stringResource(R.string.support_info), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    CustomTabsIntent.Builder()
                        .setShareState(SHARE_STATE_ON)
                        .build()
                        .launchUrl(context, "https://github.com/wbrawner/SimpleMarkdown".toUri())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.colorBackgroundGitHub)),
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.action_view_github))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val playStoreIntent = Intent(Intent.ACTION_VIEW)
                        .apply {
                            data = "market://details?id=${context.packageName}".toUri()
                            addFlags(
                                Intent.FLAG_ACTIVITY_NO_HISTORY or
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                            )
                        }
                    try {
                        startActivity(context, playStoreIntent, null)
                    } catch (_: ActivityNotFoundException) {
                        playStoreIntent.data =
                            "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                        startActivity(context, playStoreIntent, null)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.colorBackgroundPlayStore)),
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.action_rate))
            }
            SupportLinks()
        }
    }
}