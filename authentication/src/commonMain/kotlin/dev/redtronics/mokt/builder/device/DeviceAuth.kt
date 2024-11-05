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

package dev.redtronics.mokt.builder.device

import dev.redtronics.mokt.MojangGameAuth
import dev.redtronics.mokt.Provider
import dev.redtronics.mokt.html.WebTheme
import dev.redtronics.mokt.html.userCodePage
import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.device.CodeErrorResponse
import dev.redtronics.mokt.response.device.DeviceAuthStateError
import dev.redtronics.mokt.response.device.DeviceCodeResponse
import io.ktor.http.*
import io.ktor.server.cio.*
import kotlinx.html.HTML

public abstract class DeviceAuth<out T : Provider> : MojangGameAuth<T>() {
    internal var codeServer: CIOApplicationEngine? = null

    public abstract val deviceCodeEndpointUrl: Url
    public abstract val grantType: String

    /**
     * Configures the user code handling.
     *
     * @param userCode The user code to display.
     * @param builder The builder to configure the output of the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun displayCode(userCode: String, builder: suspend UserCodeBuilder.() -> Unit) {
        val userCodeBuilder = UserCodeBuilder(userCode).apply { builder() }
        codeServer = userCodeBuilder.build()
    }

    /**
     * Configures the user code handling.
     *
     * @param userCode The user code to display.
     * @param displayMode The display mode of the user code.
     * @param localServerUrl The URL to the local server.
     * @param webPageTheme The theme of the web page.
     * @param forceHttps Whether to force HTTPS.
     * @param shouldDisplayCode Whether to display the user code in the browser.
     * @param webPage The web page to display the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun displayCode(
        userCode: String,
        displayMode: DisplayMode,
        localServerUrl: Url = Url("http://localhost:18769/usercode"),
        webPageTheme: WebTheme = WebTheme.DARK,
        forceHttps: Boolean = false,
        shouldDisplayCode: Boolean = true,
        webPage: HTML.(userCode: String) -> Unit = { code -> userCodePage(code, webPageTheme) }
    ) {
        displayCode(userCode) {
            this.webPageTheme = webPageTheme
            this.webPage = webPage
            this.localServerUrl = localServerUrl
            this.forceHttps = forceHttps
            this.shouldDisplayCode = shouldDisplayCode

            if (displayMode == DisplayMode.BROWSER) {
                inBrowser()
            }
        }
    }

    public abstract suspend fun requestAuthorizationCode(onRequestError: suspend (err: CodeErrorResponse) -> Unit = {}): DeviceCodeResponse?

    public abstract suspend fun requestAccessToken(
        deviceCodeResponse: DeviceCodeResponse,
        onRequestError: suspend (err: DeviceAuthStateError)-> Unit = {}
    ): AccessResponse?

    public abstract suspend fun authLoop(
        startTime: Long,
        deviceCodeResponse: DeviceCodeResponse,
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit
    ): AccessResponse?
}
