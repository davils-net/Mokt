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

    /**
     * The url where the access token can be fetched.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract val tokenEndpointUrl: Url

    /**
     * The client id of the authentication provider and is required to start the authentication flow.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract val clientId: String

    /**
     * The client secret of the authentication provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract val clientSecret: String?

    /**
     * The scopes there are used to get the access token.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     *
     * @see Scope
     * */
    public var scopes: List<Scope> = Scope.allScopes

    /**
     * Requests the access token from the refresh token to renew the expired access token.
     *
     * @param refreshToken The refresh token from the expired access token.
     * @param onRequestError The function to be called if an error occurs during the renewal of the access token.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract suspend fun requestAccessTokenFromRefreshToken(
        refreshToken: String,
        onRequestError: suspend (response: HttpResponse) -> Unit = {}
    ): AccessResponse?
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
public suspend fun microsoftAuth(
    clientId: String,
    clientSecret: String? = getEnv("MICROSOFT_CLIENT_SECRET"),
    builder: suspend Microsoft.() -> Unit
): Microsoft = Microsoft(clientId, clientSecret).apply { builder() }

/**
 * Central adapter to communicate with the Microsoft authentication provider.
 * It creates an instance of [Microsoft] and configures it with the provided builder.
 *
 * @param clientId The client id of the authentication provider.
 * @param clientSecret The client secret of the authentication provider.
 * @param tenant The tenant of the authentication provider to specify which users can sign in to the application.
 * @param httpClient The http client used by the authentication provider.
 * @param json The json serializer used by the authentication provider.
 *
 * @return The initialized and configured [Microsoft] instance.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 *
 * @see Tenant
 */
public suspend fun microsoftAuth(
    clientId: String,
    clientSecret: String? = null,
    tenant: Tenant = Tenant.CONSUMERS,
    httpClient: HttpClient = client,
    json: Json = defaultJson
): Microsoft = microsoftAuth(clientId, clientSecret) {
    this.tenant = tenant
    this.httpClient = httpClient
    this.json = json
}

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
public suspend fun keycloakAuth(
    clientId: String,
    clientSecret: String? = getEnv("KEYCLOAK_CLIENT_SECRET"),
    realm: String,
    instanceUrl: Url,
    builder: suspend Keycloak.() -> Unit
): Keycloak = Keycloak(clientId, clientSecret, realm, instanceUrl).apply { builder() }

/**
 * Central adapter to communicate with the Keycloak authentication provider.
 * It creates an instance of [Keycloak] and configures it with the provided builder.
 *
 * @param clientId The client id of the authentication provider.
 * @param instanceUrl The base url of the keycloak instance.
 * @param realm The realm of the keycloak instance, that should be used.
 * @param clientSecret The client secret of the authentication provider.
 * @param httpClient The http client used by the authentication provider.
 * @param json The json serializer used by the authentication provider.
 *
 * @return The initialized and configured [Keycloak] instance.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public suspend fun keycloakAuth(
    clientId: String,
    instanceUrl: Url,
    realm: String,
    clientSecret: String? = null,
    httpClient: HttpClient = client,
    json: Json = defaultJson
): Keycloak = keycloakAuth(clientId, clientSecret, realm, instanceUrl) {
    this.httpClient = httpClient
    this.json = json
}
