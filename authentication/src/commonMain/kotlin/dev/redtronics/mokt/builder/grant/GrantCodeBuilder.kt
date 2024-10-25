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

import dev.redtronics.mokt.Microsoft
import dev.redtronics.mokt.MojangGameAuth
import dev.redtronics.mokt.getEnv
import dev.redtronics.mokt.html.failurePage
import dev.redtronics.mokt.html.successPage
import dev.redtronics.mokt.openInBrowser
import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.device.CodeErrorResponse
import dev.redtronics.mokt.response.grant.GrantCodeResponse
import dev.redtronics.mokt.server.codeGrantRouting
import dev.redtronics.mokt.server.setup
import dev.redtronics.mokt.utils.generateRandomIdentifier
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.html.HTML

public class GrantCodeBuilder internal constructor(override val provider: Microsoft) : MojangGameAuth<Microsoft>() {
    /**
     * The local redirect URL. On default, it will try to get the url from the environment variable `LOCAL_REDIRECT_URL`.
     * Otherwise, the url `http://localhost:8080` will be used.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var localRedirectUrl: Url = Url(getEnv("LOCAL_REDIRECT_URL") ?: "http://localhost:8080")

    public val authorizeEndpointUrl: Url
        get() = Url("https://login.microsoftonline.com/${provider.tenant.value}/oauth2/v2.0/authorize")

    /**
     * If you are not using code, you are using directly the hybrid flow.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val responseType: ResponseType = ResponseType.CODE

    public val responseMode: ResponseMode = ResponseMode.QUERY

    public var state: String = generateRandomIdentifier()

    public val grantType: String = "authorization_code"

    /**
     * Checks if the local redirect URL is using HTTPS.
     * If this is not the case, the validation check will throw an exception.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var requireHttpsByRedirect: Boolean = false

    /**
     * The page that will be shown after a successful authorization.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var successRedirectPage: HTML.() -> Unit = { successPage() }

    /**
     * The page that will be shown after a failed authorization.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var failureRedirectPage: HTML.() -> Unit = { failurePage() }

    public suspend fun requestGrantCode(
        browser: suspend (url: Url) -> Unit = { url -> openInBrowser(url) },
        onRequestError: suspend (err: CodeErrorResponse) -> Unit = {}
    ): GrantCodeResponse {
        val authCodeChannel: Channel<GrantCodeResponse> = Channel()
        val path = localRedirectUrl.fullPath.ifBlank { "/" }

        val authServer = embeddedServer(CIO, localRedirectUrl.port, localRedirectUrl.host) {
            setup()
            codeGrantRouting(path, authCodeChannel, successRedirectPage, failureRedirectPage, onRequestError)
        }
        authServer.start()

        val providerEndpointUrl = url {
            protocol = URLProtocol.HTTPS
            host = authorizeEndpointUrl.host
            path(authorizeEndpointUrl.fullPath)
            parameters.apply {
                append("client_id", provider.clientId!!)
                append("response_type", responseType.value)
                append("redirect_uri", localRedirectUrl.toString())
                append("response_mode", responseMode.value)
                append("scope", provider.scopes.joinToString(" ") { it.value })
                append("state", state)
            }
        }

        browser(Url(providerEndpointUrl))
        println("test")
        val code = authCodeChannel.receive()
        authServer.stop()

        return code
    }

    public suspend fun requestAccessToken(grantCode: GrantCodeResponse): AccessResponse? {
        val response = provider.httpClient.submitForm(
            url = provider.tokenEndpointUrl.toString(),
            parameters {
                append("client_id", provider.clientId!!)
                append("scope", provider.scopes.joinToString(" ") { it.value })
                append("code", grantCode.code)
                append("redirect_uri", localRedirectUrl.toString())
                append("grant_type", grantType)
            }
        )
        println(response.bodyAsText())
        return null
    }
}

public enum class ResponseType(public val value: String) {
    CODE("code"),
    UNKNOWN("unknown");
}

public enum class ResponseMode(public val value: String) {
    QUERY("query"),
    UNKNOWN("unknown");
}
