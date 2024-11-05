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

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Central adapter for the authentication providers.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public interface Provider {
    /**
     * The name of the authentication provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val name: String

    /**
     * The http client used by the authentication provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var httpClient: HttpClient

    /**
     * The json serializer used by the authentication provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var json: Json

    public val tokenEndpointUrl: Url

    public var clientId: String?
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
    if (microsoft.clientId == null) throw IllegalArgumentException("Client id is not set")

    require(Regex("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}").matches(microsoft.clientId!!)) { "Client id is not valid" }
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
