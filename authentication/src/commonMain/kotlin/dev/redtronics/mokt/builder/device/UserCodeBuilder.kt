/*
 * MIT License
 * Copyright 2024 Nils Jäkel & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package dev.redtronics.mokt.builder.device

import dev.redtronics.mokt.html.WebTheme
import dev.redtronics.mokt.html.userCodePage
import dev.redtronics.mokt.network.openInBrowser
import dev.redtronics.mokt.response.device.DeviceCodeResponse
import dev.redtronics.mokt.server.userCodeRouting
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.HTML

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
     * The local server url to display the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var localServerUrl: Url = Url("http://localhost:18769/usercode")

    /**
     * The web page theme for the local redirect page.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var webPageTheme: WebTheme = WebTheme.DARK

    /**
     * The rendered web page to display the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var webPage: HTML.(userCode: String) -> Unit = { userCode -> userCodePage(userCode, webPageTheme) }

    /**
     * Whenever the local redirect server should use https instead of http.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var forceHttps: Boolean = false

    /**
     * Displays the user code in the browser and opens the verification
     * url in the default web browser.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun inBrowser() {
        codeServer = embeddedServer(CIO, localServerUrl.port, localServerUrl.host) {
            val path = localServerUrl.fullPath.ifBlank { "/" }
            userCodeRouting(deviceCodeResponse.userCode, path, webPage)
        }

        codeServer!!.start()
        openInBrowser(localServerUrl, verificationUrl)
    }

    /**
     * Builds and returned the embedded server for displaying the user code.
     *
     * @return The embedded server for displaying the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal fun build(): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine. Configuration>? = codeServer
}

/**
 * The display mode for the user code.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public enum class DisplayMode {
    BROWSER,
    TERMINAL
}