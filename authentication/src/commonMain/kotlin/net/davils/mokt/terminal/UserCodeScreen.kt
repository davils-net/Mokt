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

package net.davils.mokt.terminal

import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.http.*

/**
 * Simple screen to print the user code in the terminal.
 *
 * @param userCode The user code to display
 * @param title The title of the screen
 * @param userCodeHint The step to take next.
 * @param verificationUrl The url that is needed to enter the user code.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public fun Terminal.userCodeScreen(
    userCode: String,
    title: String,
    userCodeHint: String,
    verificationUrl: Url
) {
    println(title)
    println(userCodeHint)
    println(userCode)
    println(verificationUrl.toString())
}