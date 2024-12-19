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

@file:Suppress("MemberVisibilityCanBePrivate")

package net.davils.mokt.builder.device.code

import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.http.*
import net.davils.mokt.response.device.DeviceCodeResponse
import net.davils.mokt.terminal.userCodeScreen

/**
 * Builder for the user code display in the terminal.
 *
 * @param deviceCodeResponse The device code response.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class TerminalBuilder internal constructor(private val deviceCodeResponse: DeviceCodeResponse) : Display() {
    /**
     * The default terminal to use.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var terminal: Terminal = Terminal()

    /**
     * The screen to display the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var screen: suspend Terminal.(userCode: String) -> Unit = { userCode ->
        userCodeScreen(userCode, title, userCodeHint, Url(deviceCodeResponse.verificationUri))
    }

    /**
     * Displays the user code in the terminal.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun build() {
        screen(terminal, deviceCodeResponse.userCode)
    }
}
