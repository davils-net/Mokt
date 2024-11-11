/*
 * MIT License
 * Copyright 2024 Nils Jäkel & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the “Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

package dev.redtronics.mokt.terminal

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Text
import io.ktor.http.*

public fun Terminal.userCodeScreen(
    userCode: String,
    title: String,
    userCodeHint: String,
    verificationUrl: Url
) {
    val headline = Text(
        title,
        align = TextAlign.CENTER
    )

    this.println(
        headline
    )
}