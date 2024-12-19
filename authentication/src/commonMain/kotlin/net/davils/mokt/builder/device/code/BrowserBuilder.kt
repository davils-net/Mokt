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

import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.html.HTML
import net.davils.mokt.build.BuildConstants
import net.davils.mokt.html.WebTheme
import net.davils.mokt.html.userCodePage
import net.davils.mokt.network.openInBrowser
import net.davils.mokt.response.device.DeviceCodeResponse
import net.davils.mokt.server.userCodeRouting

/**
 * Configures the browser to display the user code.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class BrowserBuilder internal constructor(
    /**
     * The device code response
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private val deviceCodeResponse: DeviceCodeResponse,

    /**
     * The verification url to enter the verification code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private val verificationUrl: Url
) : Display() {
    /**
     * The local server url where the user code is shown.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var localServerUrl: Url = Url("http://localhost:18769/usercode")

    /**
     * The url of the logo image to display.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var logoUrl: Url = Url(BuildConstants.MOKT_LOGO_URL)

    /**
     * The logo description if the logo cannot load.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var logoDescription: String = "Mokt logo"

    /**
     * The url of the background image to display.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var backgroundUrl: Url = Url(BuildConstants.MOKT_DEVICE_CODE_BACKGROUND)

    /**
     * The theme of the website [WebTheme.DARK] or [WebTheme.LIGHT].
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var theme: WebTheme = WebTheme.DARK

    /**
     * The page to display with the configured settings from the builder.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var page: HTML.(userCode: String) -> Unit = { userCode ->
        userCodePage(userCode, title, logoUrl, logoDescription, backgroundUrl, userCodeHint, theme)
    }

    /**
     * Starts the browser display.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun build(): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
        val codeServer = embeddedServer(CIO, localServerUrl.port, localServerUrl.host) {
            val path = localServerUrl.fullPath.ifBlank { "/" }
            userCodeRouting(deviceCodeResponse.userCode, path, page)
        }
        codeServer.start()

        openInBrowser(verificationUrl)
        openInBrowser(localServerUrl)

        return codeServer
    }
}