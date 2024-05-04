package com.wbrawner.md4k

object MD4K {

    init {
        System.loadLibrary("md4k")
    }

    external fun toHtml(markdown: String): String
}

fun String.toHtml() = MD4K.toHtml(this)