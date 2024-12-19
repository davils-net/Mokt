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

@file:Suppress("MemberVisibilityCanBePrivate")

package dev.redtronics.mokt.builder.device.code

import com.github.ajalt.mordant.terminal.Terminal
import dev.redtronics.mokt.html.WebTheme
import dev.redtronics.mokt.html.userCodePage
import dev.redtronics.mokt.response.device.DeviceCodeResponse
import dev.redtronics.mokt.terminal.userCodeScreen
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.HTML
import net.davils.mokt.build.BuildConstants

/**
 * Builder for displaying the user code.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class UserCodeBuilder internal constructor(
    /**
     * The device code response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private val deviceCodeResponse: DeviceCodeResponse
) {
    /**
     * The embedded http server for displaying the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private var codeServer: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

    /**
     * The verification url.
     * This is the url where the user can enter the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val verificationUrl: Url
        get() = Url(deviceCodeResponse.verificationUri)

    /**
     * Displays the user code in the browser.
     *
     * @param builder The builder to configure the user code display.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun inBrowser(builder: suspend BrowserBuilder.() -> Unit = {}) {
        val browserBuilder = BrowserBuilder(deviceCodeResponse, verificationUrl).apply { builder() }
        codeServer = browserBuilder.build()
    }

    /**
     * Displays the user code in the browser and opens the verification
     * url in the default web browser.
     *
     * @param theme The theme of the user code page.
     * @param title The title of the user code page.
     * @param userCodeHint The hint to be displayed on the user code page.
     * @param localServerUrl The url of the local server to be used.
     * @param logoUrl The url of the logo to be displayed on the user code page.
     * @param logoDescription The description of the logo to be displayed on the user code page.
     * @param backgroundUrl The url of the background image to be displayed on the user code page.
     * @param page The page to be displayed on the user code page.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun inBrowser(
        theme: WebTheme,
        title: String = "Device Code",
        userCodeHint: String = "Enter the code below in your browser",
        localServerUrl: Url = Url("http://localhost:18769/usercode"),
        logoUrl: Url = Url(BuildConstants.MOKT_LOGO_URL),
        logoDescription: String = "Mokt logo",
        backgroundUrl: Url = Url(BuildConstants.MOKT_DEVICE_CODE_BACKGROUND),
        page: HTML.(userCode: String) -> Unit = { userCode -> userCodePage(userCode, title, logoUrl, logoDescription, backgroundUrl, userCodeHint, theme) },
    ): Unit = inBrowser {
        this.title = title
        this.userCodeHint = userCodeHint
        this.localServerUrl = localServerUrl
        this.logoUrl = logoUrl
        this.logoDescription = logoDescription
        this.backgroundUrl = backgroundUrl
        this.theme = theme
        this.page = page
    }

    /**
     * Displays the user code in the terminal.
     *
     * @param builder The builder to configure the user code display.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun inTerminal(builder: suspend TerminalBuilder.() -> Unit = {}) {
        val terminalBuilder = TerminalBuilder(deviceCodeResponse).apply { builder() }
        terminalBuilder.build()
    }

    /**
     * Displays the user code in the terminal.
     *
     * @param title The title of the user code page.
     * @param userCodeHint The hint to be displayed on the user code page.
     * @param screen The screen to be displayed on the user code page.
     * @param terminal The terminal to be used.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun inTerminal(
        title: String = "Device Code",
        userCodeHint: String = "Enter the code below in your browser",
        screen: suspend Terminal.(userCode: String) -> Unit = { userCode -> userCodeScreen(userCode, title, userCodeHint, verificationUrl) },
        terminal: Terminal = Terminal()
    ): Unit = inTerminal {
        this.terminal = terminal
        this.title = title
        this.userCodeHint = userCodeHint
        this.screen = screen
    }

    /**
     * Builds and returned the embedded server for displaying the user code.
     *
     * @return The embedded server for displaying the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal fun build(): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = codeServer
}