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
import dev.redtronics.mokt.network.client
import dev.redtronics.mokt.network.defaultJson
import dev.redtronics.mokt.network.div
import dev.redtronics.mokt.response.AccessResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Keycloak authentication provider.
 * Interacts with the Keycloak API via device authentication or code grant flow.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class Keycloak internal constructor() : Provider() {
    override val name: String
        get() = "Keycloak"

    override var httpClient: HttpClient = client
    override var json: Json = defaultJson

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
    override var clientId: String? = getEnv("KEYCLOAK_CLIENT_ID")

    /**
     * The client secret for the Keycloak provider.
     * If the client secret is not set, the provider will try to get the client secret
     * from the environment `KEYCLOAK_CLIENT_SECRET.`
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override var clientSecret: String? = getEnv("KEYCLOAK_CLIENT_SECRET")

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
    public var instanceUrl: Url? = getEnv("KEYCLOAK_INSTANCE_URL")?.let { Url(it) }

    /**
     * The realm of the keycloak instance
     *
     * @throws IllegalArgumentException If the realm is not valid or not set.
     *
     * @since 0.0.1
     * @author
     * */
    public var realm: String? = null

     /**
     * The url of the microsoft's token endpoint to get the access token.
     * It would automatically resolve by on the [realm] value.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val tokenEndpointUrl: Url
        get() = instanceUrl!! / "/realms/$realm/protocol/openid-connect/token"

    /**
     * The url of the keycloak's token endpoint to get the microsoft access token.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val keycloakMsTokenEndpoint: Url
        get() = instanceUrl!! / "/realms/$realm/broker/microsoft/token"

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
        return builder(keycloakBuilder).apply { build() }
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
    public suspend fun requestMicrosoftAccessToken(
        accessResponse: AccessResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {}
    ): AccessResponse? {
        val response = httpClient.get(keycloakMsTokenEndpoint) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(accessResponse.accessToken)
            }
        }

        if (!response.status.isSuccess()) {
            onRequestError(response)
            return null
        }

        return json.decodeFromString(AccessResponse.serializer(), response.bodyAsText())
    }

    /**
     * Requests a new keycloak access token from the refresh token to renew the expired access token.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override suspend fun requestAccessTokenFromRefreshToken(
        refreshToken: String,
        onRequestError: suspend (response: HttpResponse) -> Unit,
    ): AccessResponse? {
        TODO("Not yet implemented")
    }

    /**
     * Validates the configuration of the auth provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override suspend fun build() {
        require(clientId != null && realm != null && instanceUrl != null) { "You need to set client id, realm and instance url" }
    }
}
