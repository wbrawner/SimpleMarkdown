package com.wbrawner.simplemarkdown.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.wbrawner.simplemarkdown.R

interface ShortcutHelper {
    suspend fun createEditShortcut(label: String, path: Uri): Unit
}

class AndroidShortcutHelper(private val context: Context) : ShortcutHelper {
    override suspend fun createEditShortcut(label: String, path: Uri): Unit {

        ShortcutManagerCompat.requestPinShortcut(
            context,
            ShortcutInfoCompat.Builder(
                context,
                "shortcut-to-$label"
            ).setShortLabel(label)
                .setIntent(Intent(Intent.ACTION_EDIT, path))
                .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                .build(), null
        );
    }
}