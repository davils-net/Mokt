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

public class UserCodeBuilder internal constructor(private val deviceCodeResponse: DeviceCodeResponse) {
    private var codeServer: CIOApplicationEngine? = null

    public val verificationUrl: Url
        get() = Url(deviceCodeResponse.verificationUri)

    public var localServerUrl: Url = Url("http://localhost:18769/usercode")

    public var webPageTheme: WebTheme = WebTheme.DARK

    public var webPage: HTML.(userCode: String) -> Unit = { userCode -> userCodePage(userCode, webPageTheme) }

    public var forceHttps: Boolean = false

    public var shouldDisplayCode: Boolean = true

    public suspend fun inBrowser() {
        codeServer = embeddedServer(CIO, localServerUrl.port, localServerUrl.host) {
            val path = localServerUrl.fullPath.ifBlank { "/" }
            userCodeRouting(deviceCodeResponse.userCode, path, webPage)
        }

        codeServer!!.start()
        if (shouldDisplayCode) {
            openInBrowser(localServerUrl)
        }
        openInBrowser(verificationUrl)
    }

    public suspend fun inTerminal() {

    }

    internal fun build(): CIOApplicationEngine? = codeServer
}

public enum class DisplayMode {
    BROWSER,
    TERMINAL
}