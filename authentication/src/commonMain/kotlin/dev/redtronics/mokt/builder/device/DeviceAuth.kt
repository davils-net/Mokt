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

import com.github.ajalt.mordant.terminal.Terminal
import dev.redtronics.mokt.GrantType
import dev.redtronics.mokt.OAuth
import dev.redtronics.mokt.Provider
import dev.redtronics.mokt.builder.device.code.UserCodeBuilder
import dev.redtronics.mokt.flows.AuthProgress
import dev.redtronics.mokt.flows.DeviceAuthData
import dev.redtronics.mokt.flows.FlowStep
import dev.redtronics.mokt.flows.OAuthState
import dev.redtronics.mokt.html.WebTheme
import dev.redtronics.mokt.html.userCodePage
import dev.redtronics.mokt.network.interval
import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.device.CodeErrorResponse
import dev.redtronics.mokt.response.device.DeviceAuthStateError
import dev.redtronics.mokt.response.device.DeviceAuthStateErrorItem
import dev.redtronics.mokt.response.device.DeviceCodeResponse
import dev.redtronics.mokt.terminal.userCodeScreen
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.html.HTML
import net.davils.mokt.build.BuildConstants
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
    public suspend fun displayCode(
        deviceCodeResponse: DeviceCodeResponse,
        builder: suspend UserCodeBuilder.() -> Unit,
    ) {
        val userCodeBuilder = UserCodeBuilder(deviceCodeResponse).apply { builder() }
        codeServer = userCodeBuilder.build()
    }

    /**
     * Displays the user code in the browser.
     *
     * @param deviceCodeResponse The device code response.
     * @param theme The theme to use.
     * @param localServerUrl The local server url to use.
     * @param title The title to use.
     * @param logoUrl The logo url to use.
     * @param logoDescription The logo description to use.
     * @param backgroundUrl The background url to use.
     * @param userCodeHint The user code hint to use.
     * @param page The page to display the user code.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun displayCode(
        deviceCodeResponse: DeviceCodeResponse,
        theme: WebTheme,
        localServerUrl: Url = Url("http://localhost:18769/usercode"),
        title: String = "Device Code",
        logoUrl: Url = Url(BuildConstants.MOKT_LOGO_URL),
        logoDescription: String = "Mokt logo",
        backgroundUrl: Url = Url(BuildConstants.MOKT_DEVICE_CODE_BACKGROUND),
        userCodeHint: String = "Enter the code below in your browser",
        page: HTML.(userCode: String) -> Unit = { userCode ->
            userCodePage(
                userCode,
                title,
                logoUrl,
                logoDescription,
                backgroundUrl,
                userCodeHint,
                theme
            )
        },
    ): Unit = displayCode(deviceCodeResponse) {
        inBrowser {
            this.title = title
            this.userCodeHint = userCodeHint
            this.localServerUrl = localServerUrl
            this.logoUrl = logoUrl
            this.logoDescription = logoDescription
            this.backgroundUrl = backgroundUrl
            this.page = page
        }
    }

    /**
     * Displays the user code in the terminal.
     *
     * @param deviceCodeResponse The device code response.
     * @param title The title to use.
     * @param userCodeHint The user code hint to use.
     * @param screen The screen to display the user code.
     * @param terminal The terminal to use.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun displayCode(
        deviceCodeResponse: DeviceCodeResponse,
        title: String = "Device Code",
        userCodeHint: String = "Enter the code below in your browser",
        screen: suspend Terminal.(userCode: String) -> Unit = { userCode ->
            userCodeScreen(
                userCode,
                title,
                userCodeHint,
                Url(deviceCodeResponse.verificationUri)
            )
        },
        terminal: Terminal = Terminal(),
    ): Unit = displayCode(deviceCodeResponse) {
        inTerminal {
            this.title = title
            this.userCodeHint = userCodeHint
            this.screen = screen
            this.terminal = terminal
        }
    }

    /**
     * Displays the user code as [Flow].
     *
     * @param builder The builder to configure the user code display.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : DeviceAuthData> displayCode(
        builder: suspend UserCodeBuilder.() -> Unit,
    ): FlowStep<T, AuthProgress<OAuthState>> = object : FlowStep<T, AuthProgress<OAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<OAuthState>> = channelFlow {
            send(AuthProgress(1, 2, OAuthState.DISPLAY_USER_CODE))
            if (flowData.deviceCodeResponse == null) {
                send(AuthProgress(2, 2, OAuthState.DISPLAY_USER_CODE))
                return@channelFlow
            }

            val deviceCodeResponse = flowData.deviceCodeResponse!!
            displayCode(deviceCodeResponse, builder)
            send(AuthProgress(2, 2, OAuthState.DISPLAY_USER_CODE))
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
    public suspend fun authorizationCode(
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (err: CodeErrorResponse) -> Unit = {},
    ): DeviceCodeResponse? {
        val response = submitAuthorizationForm(additionalParameters)
        if (!response.status.isSuccess()) {
            onRequestError(provider.json.decodeFromString(CodeErrorResponse.serializer(), response.bodyAsText()))
            return null
        }
        return provider.json.decodeFromString(DeviceCodeResponse.serializer(), response.bodyAsText())
    }

    /**
     * Requests the authorization code from the device code endpoint as [Flow].
     *
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the authorization code request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : DeviceAuthData> authorizationCode(
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (err: CodeErrorResponse, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
    ): FlowStep<T, AuthProgress<OAuthState>> = object : FlowStep<T, AuthProgress<OAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<OAuthState>> = channelFlow {
            send(AuthProgress(currentStep = 1, totalSteps = 2, state = OAuthState.REQUEST_USER_CODE))

            val response = submitAuthorizationForm(additionalParameters)
            if (!response.status.isSuccess()) {
                send(AuthProgress(currentStep = 2, totalSteps = 2, state = OAuthState.REQUEST_USER_CODE))
                onRequestError(
                    provider.json.decodeFromString(CodeErrorResponse.serializer(), response.bodyAsText()),
                    flowData
                )
                return@channelFlow
            }

            val code = provider.json.decodeFromString(DeviceCodeResponse.serializer(), response.bodyAsText())
            flowData.deviceCodeResponse = code
            send(AuthProgress(currentStep = 2, totalSteps = 2, state = OAuthState.REQUEST_USER_CODE))
        }
    }

    /**
     * Submits the authorization form to the device code endpoint.
     *
     * @param additionalParameters The additional parameters to be appended to the request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private suspend fun submitAuthorizationForm(
        additionalParameters: Map<String, String> = mapOf(),
    ): HttpResponse {
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
        return response
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
    public suspend fun accessToken(
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit = {},
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
    public suspend fun accessToken(
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String> = mapOf(),
        displayUserCode: suspend UserCodeBuilder.() -> Unit = {},
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit = {},
    ): AccessResponse? {
        displayCode(deviceCodeResponse, displayUserCode)
        return accessToken(deviceCodeResponse, additionalParameters, onRequestError)
    }

    /**
     * Requests the access token from the device code endpoint as [Flow].
     *
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : DeviceAuthData> accessToken(
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (err: DeviceAuthStateError, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
    ): FlowStep<T, AuthProgress<OAuthState>> = object : FlowStep<T, AuthProgress<OAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<OAuthState>> = channelFlow {
            send(AuthProgress(1, 2, OAuthState.REQUEST_ACCESS_TOKEN))
            if (flowData.deviceCodeResponse == null) {
                send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
                return@channelFlow
            }

            val deviceCodeResponse = flowData.deviceCodeResponse!!
            val startTime = getTimeMillis()
            val accessResponse = authLoop(startTime, deviceCodeResponse, additionalParameters, flowData, onRequestError)

            flowData.accessResponse = accessResponse
            send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
        }
    }

    /**
     * Requests the access token from the device code endpoint as [Flow].
     *
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : DeviceAuthData> accessToken(
        additionalParameters: Map<String, String> = mapOf(),
        displayUserCode: suspend UserCodeBuilder.() -> Unit = {},
        onRequestError: suspend (err: DeviceAuthStateError, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
    ): FlowStep<T, AuthProgress<OAuthState>> = object : FlowStep<T, AuthProgress<OAuthState>>{
        override suspend fun execute(flowData: T): Flow<AuthProgress<OAuthState>> = channelFlow {
            send(AuthProgress(1, 2, OAuthState.REQUEST_ACCESS_TOKEN))
            if (flowData.deviceCodeResponse == null) {
                send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
                return@channelFlow
            }

            val deviceCodeResponse = flowData.deviceCodeResponse!!
            displayCode(deviceCodeResponse, displayUserCode)
            val startTime = getTimeMillis()
            val accessResponse = authLoop(startTime, deviceCodeResponse, additionalParameters, flowData, onRequestError)

            flowData.accessResponse = accessResponse
            send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
        }
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
    private suspend fun authLoop(
        startTime: Long,
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String>,
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit,
    ): AccessResponse? = interval(
        interval = deviceCodeResponse.interval.seconds,
        cond = { getTimeMillis() - startTime < deviceCodeResponse.expiresIn * 1000 }
    ) {
        val response = submitAuthLoopForm(deviceCodeResponse, additionalParameters)
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

    /**
     * Interval to poll for the access token from the device token endpoint with [Flow] error handling.
     *
     * @param startTime The current start time.
     * @param deviceCodeResponse The device code response.
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param flowData The flow data.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private suspend fun <T : DeviceAuthData> authLoop(
        startTime: Long,
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String>,
        flowData: T,
        onRequestError: suspend (err: DeviceAuthStateError, flowData: T) -> Unit = { _, _ -> },
    ): AccessResponse? = interval(
        interval = deviceCodeResponse.interval.seconds,
        cond = { getTimeMillis() - startTime < deviceCodeResponse.expiresIn * 1000 }
    ) {
        val response = submitAuthLoopForm(deviceCodeResponse, additionalParameters)
        val responseBody = response.bodyAsText()

        if (responseBody.contains("error")) {
            val errorResponse = provider.json.decodeFromString(DeviceAuthStateError.serializer(), responseBody)
            if (errorResponse.error != DeviceAuthStateErrorItem.AUTHORIZATION_PENDING) {
                onRequestError(errorResponse, flowData)
                cancel()
            }
            return@interval null
        }

        codeServer?.stop()
        return@interval provider.json.decodeFromString(AccessResponse.serializer(), responseBody)
    }

    /**
     * Submits the authorization form to the device code endpoint.
     *
     * @param deviceCodeResponse The device code response.
     * @param additionalParameters The additional parameters to be appended to the request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private suspend fun submitAuthLoopForm(
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String>,
    ): HttpResponse {
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
        return response
    }
}
