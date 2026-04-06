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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wbrawner.simplemarkdown.R
import com.wbrawner.simplemarkdown.core.ui.SupportButton
import com.wbrawner.simplemarkdown.core.ui.theme.SimpleMarkdownTheme
import com.wbrawner.simplemarkdown.utility.SupportLinks
import com.wbrawner.simplemarkdown.core.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavController) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MarkdownTopAppBar(
                title = stringResource(R.string.support_title),
                goBack = { navController.popBackStack() },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(100.dp),
                painter = painterResource(R.drawable.favorite_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(stringResource(R.string.support_info), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            SupportButton(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.patreon,
                title = R.string.action_become_patron,
                contentColor = Color.White,
                containerColor = Color.Black,
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://www.patreon.com/cw/wbrawner".toUri()
                        )
                    )
                },
            )
            SupportButton(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.liberapay_logo,
                title = R.string.action_donate_liberapay,
                contentColor = Color.Black,
                containerColor = colorResource(R.color.liberapay_background),
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://liberapay.com/wbrawner/".toUri()
                        )
                    )
                },
            )
            SupportButton(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.github,
                title = R.string.action_github_sponsor,
                contentColor = Color.White,
                containerColor = colorResource(CoreR.color.colorBackgroundGitHub),
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/sponsors/wbrawner".toUri()
                        )
                    )
                },
            )
            SupportLinks()
            SupportButton(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.github,
                title = R.string.action_view_github,
                contentColor = Color.White,
                containerColor = colorResource(CoreR.color.colorBackgroundGitHub),
                onClick = {
                    CustomTabsIntent.Builder()
                        .setShareState(SHARE_STATE_ON)
                        .build()
                        .launchUrl(context, "https://github.com/wbrawner/SimpleMarkdown".toUri())
                },
            )
            SupportButton(
                modifier = Modifier.fillMaxWidth(),
                icon = CoreR.drawable.rate_review,
                title = R.string.action_rate,
                contentColor = Color.White,
                containerColor = colorResource(CoreR.color.colorBackgroundPlayStore),
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
                        context.startActivity(playStoreIntent, null)
                    } catch (_: ActivityNotFoundException) {
                        playStoreIntent.data =
                            "https://play.google.com/store/apps/details?id=com.wbrawner.simplemarkdown".toUri()
                        context.startActivity(playStoreIntent, null)
                    }
                },
            )
        }
    }
}

@Composable
@PreviewLightDark
fun SupportScreen_Preview() {
    SimpleMarkdownTheme(useAmoledDarkTheme = false) {
        SupportScreen(rememberNavController())
    }
}