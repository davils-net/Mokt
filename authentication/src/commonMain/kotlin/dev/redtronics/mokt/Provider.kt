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

import dev.redtronics.mokt.flows.*
import dev.redtronics.mokt.network.client
import dev.redtronics.mokt.network.defaultJson
import dev.redtronics.mokt.response.AccessResponse
import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
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
     * Requests an access token from the given refresh token.
     *
     * @param refreshToken The refresh token.
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun accessTokenFromRefreshToken(
        refreshToken: String,
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
    ): AccessResponse? {
        val response = submitAccessForm(refreshToken, additionalParameters)
        if (!response.status.isSuccess()) {
            onRequestError(response)
            return null
        }

        return json.decodeFromString(AccessResponse.serializer(), response.bodyAsText())
    }

    /**
     * Requests an access token from the given refresh token.
     *
     * @param refreshToken The refresh token.
     * @param additionalParameters The additional parameters to be appended to the request.
     * @param onRequestError The function to be called if an error occurs during the access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : AuthData> accessTokenFromRefreshToken(
        refreshToken: String,
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit = { _, flowData: T -> flowData.cancel() },
    ): FlowStep<T, AuthProgress<OAuthState>> = object : FlowStep<T, AuthProgress<OAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<OAuthState>> = channelFlow {
            send(AuthProgress(1, 2, OAuthState.REQUEST_ACCESS_TOKEN))
            val response = submitAccessForm(refreshToken, additionalParameters)
            if (!response.status.isSuccess()) {
                onRequestError(response, flowData)
                return@channelFlow
            }

            val accessResponse = json.decodeFromString(AccessResponse.serializer(), response.bodyAsText())
            flowData.accessResponse = accessResponse
            send(AuthProgress(2, 2, OAuthState.REQUEST_ACCESS_TOKEN))
        }
    }

    /**
     * Requests an access token from the given refresh token.
     *
     * @param refreshToken The refresh token.
     * @param additionalParameters The additional parameters to be appended to the request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private suspend fun submitAccessForm(
        refreshToken: String,
        additionalParameters: Map<String, String> = mapOf(),
    ): HttpResponse {
        val response = httpClient.submitForm(
            url = tokenEndpointUrl.toString(),
            formParameters = parameters {
                append("client_id", clientId)
                append("scope", scopes.joinToString(" ") { it.value })
                append("refresh_token", refreshToken)
                append("grant_type", GrantType.REFRESH_TOKEN.value)

                if (clientSecret != null) {
                    append("client_secret", clientSecret!!)
                }

                additionalParameters.forEach { (key, value) ->
                    append(key, value)
                }
            }
        )
        return response
    }

    /**
     * Requests an access token from the given refresh token.
     *
     * @param data The data to be used in the flow.
     * @param refreshToken The refresh token.
     * @param steps The steps to be executed in the flow.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun <T : AuthData> refreshFlow(
        data: T,
        refreshToken: String,
        vararg steps: FlowStep<T, AuthProgress<OAuthState>> = arrayOf(accessTokenFromRefreshToken<T>(refreshToken)),
    ): Flow<AuthProgress<OAuthState>> = flow(data) {
        steps.forEach {
            step(it)
        }
    }
}

/**
 * Central adapter to communicate with the Microsoft authentication provider.
 * It creates an instance of [Microsoft] and configures it with the provided builder.
 *
 * @param clientId The client id of the authentication provider.
 * @param clientSecret The client secret of the authentication provider. If null, the client secret is try to get from the environment `MICROSOFT_CLIENT_SECRET`.
 * @param builder The builder to configure the [Microsoft] instance.
 * @return The initialized and configured [Microsoft] instance.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public suspend fun microsoftAuth(
    clientId: String,
    clientSecret: String? = getEnv("MICROSOFT_CLIENT_SECRET"),
    builder: suspend Microsoft.() -> Unit,
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
    json: Json = defaultJson,
): Microsoft = microsoftAuth(clientId, clientSecret) {
    this.tenant = tenant
    this.httpClient = httpClient
    this.json = json
}

/**
 * Central adapter to communicate with the Keycloak authentication provider.
 * It creates an instance of [Keycloak] and configures it with the provided builder.
 *
 * @param clientId The client id of the authentication provider.
 * @param clientSecret The client secret of the authentication provider. If null, the client secret is try to get from the environment `KEYCLOAK_CLIENT_SECRET`.
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
    builder: suspend Keycloak.() -> Unit,
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
    clientSecret: String? = null,
    realm: String,
    instanceUrl: Url,
    httpClient: HttpClient = client,
    json: Json = defaultJson,
): Keycloak = keycloakAuth(clientId, clientSecret, realm, instanceUrl) {
    this.httpClient = httpClient
    this.json = json
}
