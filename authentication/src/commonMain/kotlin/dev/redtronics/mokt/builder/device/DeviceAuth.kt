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

import dev.redtronics.mokt.GrantType
import dev.redtronics.mokt.OAuth
import dev.redtronics.mokt.Provider
import dev.redtronics.mokt.build.BuildConstants
import dev.redtronics.mokt.html.WebTheme
import dev.redtronics.mokt.network.interval
import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.device.CodeErrorResponse
import dev.redtronics.mokt.response.device.DeviceAuthStateError
import dev.redtronics.mokt.response.device.DeviceAuthStateErrorItem
import dev.redtronics.mokt.response.device.DeviceCodeResponse
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.date.*
import kotlinx.html.HTML
import kotlin.time.Duration.Companion.seconds

/**
 * Base class for all device authentication.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public abstract class DeviceAuth<out T : Provider> internal constructor() : OAuth<T>() {
    override val grantType: GrantType = GrantType.DEVICE_CODE

    /**
     * The local code redirect server to display the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private var codeServer: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null


    /**
     * Endpoint to request the device and user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract val deviceCodeEndpointUrl: Url

    /**
     * Displays the user code to the user.
     *
     * @param deviceCodeResponse The device code response.
     * @param builder The builder to configure the user code display.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun displayCode(deviceCodeResponse: DeviceCodeResponse, builder: suspend UserCodeBuilder.() -> Unit) {
        val userCodeBuilder = UserCodeBuilder(deviceCodeResponse).apply { builder() }
        codeServer = userCodeBuilder.build()
    }

    /**
     * Displays the user code to the user.
     *
     * @param webPageOverride The function to override the default web page.
     * @param deviceCodeResponse The device code response.
     * @param displayMode The display mode to use.
     * @param localServerUrl The local server url to use.
     * @param title The title to use.
     * @param logoUrl The logo url to use.
     * @param logoDescription The logo description to use.
     * @param backgroundUrl The background url to use.
     * @param userCodeHint The user code hint to use.
     * @param theme The theme to use.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun displayCode(
        webPageOverride: (HTML.(userCode: String) -> Unit)? = null,
        deviceCodeResponse: DeviceCodeResponse,
        displayMode: DisplayMode = DisplayMode.BROWSER,
        localServerUrl: Url = Url("http://localhost:18769/usercode"),
        title: String = "Device Code",
        logoUrl: Url = Url(BuildConstants.MOKT_LOGO_URL),
        logoDescription: String = "Mokt logo",
        backgroundUrl: Url = Url(BuildConstants.MOKT_DEVICE_CODE_BACKGROUND),
        userCodeHint: String = "Enter the code below in your browser",
        theme: WebTheme = WebTheme.DARK,
    ): Unit = displayCode(deviceCodeResponse) {
        this.localServerUrl = localServerUrl

        if (displayMode == DisplayMode.BROWSER) {
            inBrowser(webPageOverride, title, logoUrl, logoDescription, backgroundUrl, userCodeHint, theme)
        } else {
            inTerminal()
        }
    }

    /**
     * Requests the authorization code from the device code endpoint.
     *
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the authorization code request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun requestAuthorizationCode(
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (err: CodeErrorResponse) -> Unit = {}
    ): DeviceCodeResponse? {
        val response = provider.httpClient.submitForm(
            url = deviceCodeEndpointUrl.toString(),
            formParameters = parameters {
                append("client_id", provider.clientId)
                append("scope", provider.scopes.joinToString(" ") { it.value })

                if (provider.clientSecret != null) {
                    append("client_secret", provider.clientSecret!!)
                }

                additionalParameters.forEach {
                    append(it.key, it.value)
                }
            }
        )
        if (!response.status.isSuccess()) {
            onRequestError(provider.json.decodeFromString(CodeErrorResponse.serializer(), response.bodyAsText()))
            return null
        }
        return provider.json.decodeFromString(DeviceCodeResponse.serializer(), response.bodyAsText())
    }

    /**
     * Requests the access token from the device code endpoint.
     *
     * @param deviceCodeResponse The device code response.
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun requestAccessToken(
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit = {}
    ): AccessResponse? {
        val startTime = getTimeMillis()
        return authLoop(startTime, deviceCodeResponse, additionalParameters, onRequestError)
    }

    /**
     * Requests the access token from the device code endpoint.
     *
     * @param deviceCodeResponse The device code response.
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun requestAccessToken(
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String> = mapOf(),
        displayUserCode: suspend UserCodeBuilder.() -> Unit = {},
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit = {}
    ): AccessResponse? {
        displayCode(deviceCodeResponse, displayUserCode)
        return requestAccessToken(deviceCodeResponse, additionalParameters, onRequestError)
    }

    /**
     * Interval to poll for the access token from the device token endpoint.
     *
     * @param startTime The current start time.
     * @param deviceCodeResponse The device code response.
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun authLoop(
        startTime: Long,
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String>,
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit
    ): AccessResponse? = interval(
        interval = deviceCodeResponse.interval.seconds,
        cond = { getTimeMillis() - startTime < deviceCodeResponse.expiresIn * 1000 }
    ) {
        val response = provider.httpClient.submitForm(
            url = provider.tokenEndpointUrl.toString(),
            formParameters = parameters {
                append("client_id", provider.clientId)
                append("device_code", deviceCodeResponse.deviceCode)
                append("grant_type", grantType.value)

                if (provider.clientSecret != null) {
                    append("client_secret", provider.clientSecret!!)
                }

                additionalParameters.forEach {
                    append(it.key, it.value)
                }
            }
        )

        val responseBody = response.bodyAsText()
        if (responseBody.contains("error")) {
            val errorResponse = provider.json.decodeFromString(DeviceAuthStateError.serializer(), responseBody)
            if (errorResponse.error != DeviceAuthStateErrorItem.AUTHORIZATION_PENDING) {
                onRequestError(errorResponse)
                cancel()
            }
            return@interval null
        }

        codeServer?.stop()
        return@interval provider.json.decodeFromString(AccessResponse.serializer(), responseBody)
    }


}
