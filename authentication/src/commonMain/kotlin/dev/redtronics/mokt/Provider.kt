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

import dev.redtronics.mokt.network.client
import dev.redtronics.mokt.network.defaultJson
import dev.redtronics.mokt.response.AccessResponse
import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Central adapter for the authentication providers.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public abstract class Provider {
    /**
     * The name of the authentication provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract val name: String

    /**
     * The http client used by the authentication provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract var httpClient: HttpClient

    /**
     * The json serializer used by the authentication provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract var json: Json

    public abstract val tokenEndpointUrl: Url

    public abstract var clientId: String?

    public abstract var clientSecret: String?

    public var scopes: List<Scope> = Scope.allScopes

    public abstract suspend fun requestAccessTokenFromRefreshToken(
        accessResponse: AccessResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {}
    ): AccessResponse?

    internal abstract suspend fun build()
}

/**
 * Central adapter to communicate with the Microsoft authentication provider.
 * It creates an instance of [Microsoft] and configures it with the provided builder.
 *
 * @param builder The builder to configure the [Microsoft] instance.
 * @return The initialized and configured [Microsoft] instance.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public suspend fun microsoftAuth(builder: suspend Microsoft.() -> Unit): Microsoft {
    val microsoft = Microsoft().apply { builder() }
    return microsoft
}

/**
 * Central adapter to communicate with the Mojang authentication provider.
 * It creates an instance of [Mojang] and configures it with the provided builder.
 *
 * This provider is useful if you write your own authentication provider to
 * get the microsoft access token.
 * It means this provider cannot get the microsoft access token in any way.
 * You must implement this with your own provider.
 *
 *
 * @param builder The builder to configure the [Microsoft] instance.
 * @return The initialized and configured [Microsoft] instance.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
//public suspend fun mojangAuth(builder: suspend Mojang.() -> Unit): Mojang {
//    val mojang = Mojang().apply { builder() }
//    return mojang
//}

/**
 * Central adapter to communicate with the Keycloak authentication provider.
 * It creates an instance of [Keycloak] and configures it with the provided builder.
 *
 * @param builder The builder to configure the [Keycloak] instance.
 * @return The initialized and configured [Keycloak] instance.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public suspend fun keycloakAuth(builder: suspend Keycloak.() -> Unit): Keycloak {
    val keycloak = Keycloak().apply { builder() }
    return keycloak
}

public suspend fun keycloakAuth(
    clientId: String,
    instanceUrl: Url,
    realm: String,
    httpClient: HttpClient = client,
    json: Json = defaultJson
): Keycloak = keycloakAuth {
    this.clientId = clientId
    this.instanceUrl = instanceUrl
    this.realm = realm
    this.httpClient = httpClient
    this.json = json
}