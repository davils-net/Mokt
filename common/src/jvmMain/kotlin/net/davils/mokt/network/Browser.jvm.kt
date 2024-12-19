/*
 * MIT License
 * Copyright 2024 Nils Jäkel  & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

package net.davils.mokt.network

import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.davils.mokt.OsType
import net.davils.mokt.os
import java.awt.Desktop

public actual suspend fun openInBrowser(url: Url): Unit = withContext(Dispatchers.IO) {
    when (os) {
        OsType.WINDOWS -> {
            Desktop.getDesktop().browse(url.toURI())
        }

        OsType.MACOS -> {
            val process = ProcessBuilder("open", url.toString())
            process.start()
        }

        else -> {
            val process = ProcessBuilder("xdg-open", url.toString())
            process.start()
        }
    }
}

