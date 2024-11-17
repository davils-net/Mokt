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

package dev.redtronics.mokt.builder.grant

import dev.redtronics.mokt.*
import dev.redtronics.mokt.flows.*
import dev.redtronics.mokt.html.redirectPage
import dev.redtronics.mokt.html.style.Color
import dev.redtronics.mokt.network.openInBrowser
import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.GrantCodeResponse
import dev.redtronics.mokt.response.device.CodeErrorResponse
import dev.redtronics.mokt.server.grantRouting
import dev.redtronics.mokt.server.grantRoutingWithFlow
import dev.redtronics.mokt.server.setup
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.html.HTML

/**
 * Abstract class for grant authentication.
 * Contains the necessary properties and methods for the grant authentication flow.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public abstract class GrantAuth<out T : Provider> internal constructor() : OAuth<T>() {
    override val grantType: GrantType = GrantType.AUTHORIZATION_CODE

    /**
     * The authorize endpoint URL.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract val authorizeEndpointUrl: Url

    /**
     * The local redirect URL. On default, it will try to get the url from the environment variable `LOCAL_REDIRECT_URL`.
     * Otherwise, the url `http://localhost:8080` will be used.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var localRedirectUrl: Url = Url(getEnv("LOCAL_REDIRECT_URL") ?: "http://localhost:8080")

    /**
     * The response type for the code request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val responseType: ResponseType = ResponseType.CODE

    /**
     * The response mode for the code request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val responseMode: ResponseMode = ResponseMode.QUERY

    /**
     * Unique identifier for the request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var state: String = generateRandomIdentifier()

    /**
     * The page that will be shown after a successful authorization.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var successRedirectPage: HTML.() -> Unit = {
        redirectPage(
            "Authentication successful",
            "You can close this page now!",
            Color("#ffffff"),
            Color("#009320"),
            Color("#009320")
        )
    }

    /**
     * The page that will be shown after a failed authorization.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var failureRedirectPage: HTML.() -> Unit = {
        redirectPage(
            "Authentication failed",
            "Please try again!",
            Color("#ffffff"),
            Color("#ff0000"),
            Color("#b20000")
        )
    }

    /**
     * Requests the authorization code from the authorization server.
     *
     * @param browser The function to open the browser.
     * @param onRequestError The function to be called if an error occurs during the authorization code request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun grantCode(
        browser: suspend (url: Url) -> Unit = { url -> openInBrowser(url) },
        onRequestError: suspend (err: CodeErrorResponse) -> Unit = {},
    ): GrantCodeResponse? {
        val authCodeChannel: Channel<GrantCodeResponse?> = Channel()
        val path = localRedirectUrl.fullPath.ifBlank { "/" }
        val authServer = embeddedServer(CIO, localRedirectUrl.port, localRedirectUrl.host) {
            setup()
            grantRouting(path, authCodeChannel, successRedirectPage, failureRedirectPage, onRequestError)
        }
        authServer.start()

        val code = submitGrantCodeForm(authCodeChannel, authServer, browser)
        return code
    }

    /**
     * Requests the authorization code from the authorization server.
     *
     * @param browser The function to open the browser.
     * @param onRequestError The function to be called if an error occurs during the authorization code request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : GrantAuthData> grantCode(
        browser: suspend (url: Url) -> Unit = { url -> openInBrowser(url) },
        onRequestError: suspend (err: CodeErrorResponse, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
    ): FlowStep<T, AuthProgress<OAuthState>> = object : FlowStep<T, AuthProgress<OAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<OAuthState>> = channelFlow {
            send(AuthProgress(currentStep = 1, totalSteps = 2, state = OAuthState.REQUEST_GRANT_CODE))

            val authCodeChannel: Channel<GrantCodeResponse?> = Channel()
            val path = localRedirectUrl.fullPath.ifBlank { "/" }
            val authServer = embeddedServer(CIO, localRedirectUrl.port, localRedirectUrl.host) {
                setup()
                grantRoutingWithFlow(
                    path,
                    authCodeChannel,
                    successRedirectPage,
                    failureRedirectPage,
                    flowData,
                    onRequestError
                )
            }
            authServer.start()

            val code = submitGrantCodeForm(authCodeChannel, authServer, browser)
            if (code == null) {
                send(AuthProgress(currentStep = 2, totalSteps = 2, state = OAuthState.REQUEST_GRANT_CODE))
                return@channelFlow
            }

            flowData.grantCodeResponse = code
            send(AuthProgress(currentStep = 2, totalSteps = 2, state = OAuthState.REQUEST_GRANT_CODE))
        }
    }

    /**
     * Requests the authorization code from the authorization server.
     *
     * @param channel The channel to receive the authorization code response.
     * @param authServer The started embedded server.
     * @param browser The function to open the browser.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private suspend fun submitGrantCodeForm(
        channel: Channel<GrantCodeResponse?>,
        authServer: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>,
        browser: suspend (url: Url) -> Unit = { url -> openInBrowser(url) },
    ): GrantCodeResponse? {
        val providerEndpointUrl = url {
            protocol = URLProtocol.HTTPS
            host = authorizeEndpointUrl.host
            path(authorizeEndpointUrl.fullPath)
            parameters.apply {
                append("client_id", provider.clientId)
                append("response_type", responseType.value)
                append("redirect_uri", localRedirectUrl.toString())
                append("response_mode", responseMode.value)
                append("scope", provider.scopes.joinToString(" ") { it.value })
                append("state", state)
            }
        }
        browser(Url(providerEndpointUrl))

        val code = channel.receive()
        authServer.stop()
        return code
    }

    /**
     * Requests the access token from the token endpoint.
     *
     * @param grantCodeResponse The grant code response.
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun accessToken(
        grantCodeResponse: GrantCodeResponse,
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
    ): AccessResponse? {
        val response = submitAccessForm(grantCodeResponse, additionalParameters)
        if (!response.status.isSuccess()) {
            onRequestError(response)
            return null
        }
        return provider.json.decodeFromString(AccessResponse.serializer(), response.bodyAsText())
    }

    /**
     * Requests the access token from the token endpoint as [Flow].
     *
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : GrantAuthData> accessToken(
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
    ): FlowStep<T, AuthProgress<OAuthState>> = object : FlowStep<T, AuthProgress<OAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<OAuthState>> = channelFlow {
            send(AuthProgress(1, 2, OAuthState.REQUEST_ACCESS_TOKEN))
            if (flowData.grantCodeResponse == null) {
                send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
                return@channelFlow
            }

            val response = submitAccessForm(flowData.grantCodeResponse!!, additionalParameters)
            if (!response.status.isSuccess()) {
                send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
                onRequestError(response, flowData)
                return@channelFlow
            }

            send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
        }
    }

    /**
     * Requests the access token from the token endpoint.
     *
     * @param grantCodeResponse The grant code response.
     * @param additionalParameters The additional parameters to be appended to the request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private suspend fun submitAccessForm(
        grantCodeResponse: GrantCodeResponse,
        additionalParameters: Map<String, String> = mapOf(),
    ): HttpResponse {
        val response = provider.httpClient.submitForm(
            url = provider.tokenEndpointUrl.toString(),
            parameters {
                append("client_id", provider.clientId)
                append("scope", provider.scopes.joinToString(" ") { it.value })
                append("code", grantCodeResponse.code)
                append("redirect_uri", localRedirectUrl.toString())
                append("grant_type", grantType.value)
                append("state", state)

                if (provider.clientSecret != null) {
                    append("client_secret", provider.clientSecret!!)
                }

                additionalParameters.forEach { (key, value) ->
                    append(key, value)
                }
            }
        )
        return response
    }
}

/**
 * Possible response types for the authorization request.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public enum class ResponseType(public val value: String) {
    CODE("code"),
    UNKNOWN("unknown");
}

/**
 * Possible response modes for the authorization request.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public enum class ResponseMode(public val value: String) {
    QUERY("query"),
    UNKNOWN("unknown");
}
