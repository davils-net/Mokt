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

public class Keycloak internal constructor() : Provider() {
    override val name: String
        get() = "Keycloak"

    override var httpClient: HttpClient = client

    override var json: Json = defaultJson

    override var clientId: String? = getEnv("KEYCLOAK_CLIENT_ID")

    override var clientSecret: String? = getEnv("KEYCLOAK_CLIENT_SECRET")

    public var instanceUrl: Url? = null

    public var realm: String? = null

    override val tokenEndpointUrl: Url
        get() = instanceUrl!! / "/realms/$realm/protocol/openid-connect/token"

    public val microsoftTokenEndpoint: Url
        get() = instanceUrl!! / "/realms/$realm/broker/microsoft/token"


    public suspend fun <T> device(builder: suspend KeycloakDeviceBuilder.() -> T): T {
        val keycloakBuilder = KeycloakDeviceBuilder(this)
        return builder(keycloakBuilder).apply { build() }
    }

    public suspend fun requestMicrosoftAccessToken(
        accessResponse: AccessResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {}
    ): AccessResponse? {
        val response = httpClient.get(microsoftTokenEndpoint) {
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

    override suspend fun requestAccessTokenFromRefreshToken(
        accessResponse: AccessResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit,
    ): AccessResponse? {
        TODO("Not yet implemented")
    }

    override suspend fun build() {
        require(clientId != null && realm != null && instanceUrl != null) { "You need to set client id, realm and instance url" }
    }
}
