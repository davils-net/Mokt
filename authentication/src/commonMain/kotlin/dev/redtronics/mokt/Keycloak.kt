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

package dev.redtronics.mokt

import dev.redtronics.mokt.builder.device.KeycloakDeviceBuilder
import dev.redtronics.mokt.builder.grant.KeycloakGrantBuilder
import dev.redtronics.mokt.flows.*
import dev.redtronics.mokt.network.client
import dev.redtronics.mokt.network.defaultJson
import dev.redtronics.mokt.network.div
import dev.redtronics.mokt.response.AccessResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json

/**
 * Keycloak authentication provider.
 * Interacts with the Keycloak API via device authentication or code grant flow.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class Keycloak internal constructor(
    /**
     * The client id for the Keycloak provider.
     * If the client id is not set, the provider will try to get the client id
     * from the environment `KEYCLOAK_CLIENT_ID.`
     *
     * @throws IllegalArgumentException If the client id is not valid or not set.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override var clientId: String,

    /**
     * The client secret for the Keycloak provider.
     * If the client secret is not set, the provider will try to get the client secret
     * from the environment `KEYCLOAK_CLIENT_SECRET.`
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override var clientSecret: String?,

    /**
     * The realm of the keycloak instance
     *
     * @throws IllegalArgumentException If the realm is not valid or not set.
     *
     * @since 0.0.1
     * @author
     * */
    public val realm: String,

    /**
     * The base url of the keycloak instance.
     * If the instance url is not set, the provider will try to get the instance url
     * from the environment `KEYCLOAK_INSTANCE_URL.`
     *
     * @throws IllegalArgumentException If the instance url is not valid or not set.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val instanceUrl: Url
) : Provider() {
    override val name: String
        get() = "Keycloak"

    override var httpClient: HttpClient = client
    override var json: Json = defaultJson

    /**
     * The url of the microsoft's token endpoint to get the access token.
     * It would automatically resolve by on the [realm] value.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val tokenEndpointUrl: Url
        get() = instanceUrl / "/realms/$realm/protocol/openid-connect/token"

    /**
     * The url of the keycloak's token endpoint to get the microsoft access token.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val keycloakMsTokenEndpoint: Url
        get() = instanceUrl / "/realms/$realm/broker/microsoft/token"

    /**
     * Uses keycloak's device authentication flow.
     *
     * The OAuth 2.0 device authorization grant is designed for Internet
     * connected devices that either lack a browser to perform a user-agent-
     * based authorization or are input constrained to the extent that
     * requiring the user to input text in order to authenticate during the
     * authorization flow is impractical.
     *
     * It enables OAuth clients on such
     * devices (like smart TVs, media consoles, digital picture frames, and
     * printers) to obtain user authorization to access protected resources
     * by using a user agent on a separate device.
     *
     * @param builder The builder to configure the device flow.
     * @return The result of the builder [T].
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun <T> device(builder: suspend KeycloakDeviceBuilder.() -> T): T {
        val keycloakBuilder = KeycloakDeviceBuilder(this)
        return builder(keycloakBuilder)
    }

    /**
     * Uses keycloak's code grant flow.
     *
     * The OAuth 2.0 authorization code grant, also known as the authorization code flow,
     * enables a client application to obtain authorized access to protected resources,
     * such as web APIs.
     *
     * The authorization code flow requires a user agent that supports redirection from
     * the authorization server (Keycloak Instance) back to your application.
     * This can be a web browser, a desktop application, or a mobile application that
     * allows a user to sign in to your application and access their data.
     *
     * @param builder The builder to configure the OAuth 2.0 flow.
     * @return The last result of the builder [T].
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun <T> grant(builder: suspend KeycloakGrantBuilder.() -> T): T {
        val keycloakBuilder = KeycloakGrantBuilder(this)
        return builder(keycloakBuilder)
    }

    public fun grantFlow() {

    }

    public fun deviceFlow() {

    }

    /**
     * Requests the microsoft access token from the keycloak token endpoint.
     *
     * @param accessResponse The keycloak access token response.
     * @param onRequestError The function to be called if an error occurs during the microsoft access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun microsoftAccessToken(
        accessResponse: AccessResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
    ): AccessResponse? {
        val response = submitMicrosoftAccessForm(accessResponse)
        if (!response.status.isSuccess()) {
            onRequestError(response)
            return null
        }

        return json.decodeFromString(AccessResponse.serializer(), response.bodyAsText())
    }

    /**
     * Requests the microsoft access token from the keycloak token endpoint.
     *
     * @param onRequestError The function to be called if an error occurs during the microsoft access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : KeycloakAuthData> microsoftAccessToken(
        onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
    ): FlowStep<T, AuthProgress<OAuthState>> = object : FlowStep<T, AuthProgress<OAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<OAuthState>> = channelFlow {
            send(AuthProgress(1, 2, OAuthState.REQUEST_ACCESS_TOKEN))
            if (flowData.accessResponse == null) {
                send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
                return@channelFlow
            }

            val accessResponse = flowData.accessResponse!!
            val response = submitMicrosoftAccessForm(accessResponse)
            if (!response.status.isSuccess()) {
                send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
                onRequestError(response, flowData)
                return@channelFlow
            }

            val microsoftAccessResponse= json.decodeFromString(AccessResponse.serializer(), response.bodyAsText())
            flowData.microsoftAccessResponse = microsoftAccessResponse
            send(AuthProgress(1, 2, OAuthState.REQUEST_ACCESS_TOKEN))
        }
    }

    /**
     * Requests the microsoft access token from the keycloak token endpoint.
     *
     * @param accessResponse The keycloak access token response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private suspend fun submitMicrosoftAccessForm(
        accessResponse: AccessResponse
    ): HttpResponse {
        val response = httpClient.get(keycloakMsTokenEndpoint) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(accessResponse.accessToken)
            }
        }
        return response
    }
}
