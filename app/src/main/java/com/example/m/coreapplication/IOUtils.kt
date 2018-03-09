package com.example.m.coreapplication

import java.io.InputStream
import java.nio.charset.Charset

object IOUtils {

    fun readTextAndClose(inputStream: InputStream,
                         charset: Charset = Charsets.UTF_8): String =
            inputStream.bufferedReader(charset).use { it.readText() }
}