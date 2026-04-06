package com.wbrawner.simplemarkdown.core.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.wbrawner.simplemarkdown.core.R
import com.wbrawner.simplemarkdown.core.ui.theme.SimpleMarkdownTheme

@Composable
fun SupportButton(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    containerColor: Color = MaterialTheme.colorScheme.primary,
) {
    SupportButton(
        modifier = modifier,
        icon = icon,
        title = stringResource(title),
        contentColor = contentColor,
        containerColor = containerColor,
        onClick = onClick
    )
}

@Composable
fun SupportButton(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    containerColor: Color = MaterialTheme.colorScheme.primary,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColor
        )
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@PreviewLightDark
fun SupportButton_Preview() {
    SimpleMarkdownTheme(useAmoledDarkTheme = false) {
        SupportButton(
            modifier = Modifier.fillMaxWidth(),
            title = R.string.action_preview,
            icon = R.drawable.rate_review,
            onClick = {},
        )
    }
}