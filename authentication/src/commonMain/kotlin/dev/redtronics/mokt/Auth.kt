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

import dev.redtronics.mokt.builder.mojang.MojangBuilder
import dev.redtronics.mokt.builder.mojang.XBoxBuilder
import dev.redtronics.mokt.builder.mojang.XstsBuilder
import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.mojang.MojangResponse
import dev.redtronics.mokt.response.mojang.XBoxResponse
import dev.redtronics.mokt.response.mojang.XstsResponse
import io.ktor.client.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

/**
 * Central adapter for the authentication providers.
 *
 * @property name The name of the provider.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public interface Provider {
    public val name: String
    public var httpClient: HttpClient
    public var json: Json
}

public abstract class MojangGameAuth<out T : Provider> {
    public abstract val provider: T

    public suspend fun xBox(
        accessResponse: AccessResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
        builder: suspend XBoxBuilder.() -> Unit = {},
    ): XBoxResponse? {
        val xBoxBuilder = XBoxBuilder(provider.httpClient, provider.json, accessResponse).apply { builder() }
        return xBoxBuilder.build(onRequestError)
    }

    public suspend fun xsts(
        xBoxResponse: XBoxResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
        builder: suspend XstsBuilder.() -> Unit,
    ): XstsResponse? {
        val xstsBuilder = XstsBuilder(provider.httpClient, provider.json, xBoxResponse).apply { builder() }
        return xstsBuilder.build(onRequestError)
    }

    public suspend fun xsts(
        xBoxResponse: XBoxResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
        relyingParty: String = "rp://api.minecraftservices.com/",
    ): XstsResponse? {
        val xsts = xsts(xBoxResponse, onRequestError) {
            this.relyingParty = relyingParty
        }
        return xsts
    }

    public suspend fun mojang(
        xstsResponse: XstsResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
        builder: suspend MojangBuilder.() -> Unit = {},
    ): MojangResponse? {
        val mojangBuilder = MojangBuilder(provider.httpClient, provider.json, xstsResponse).apply { builder() }
        return mojangBuilder.build(onRequestError)
    }
}

public suspend fun microsoftAuth(builder: suspend Microsoft.() -> Unit): Microsoft {
    val microsoft = Microsoft().apply { builder() }
    if (microsoft.clientId == null) throw IllegalArgumentException("Client id is not set")

    require(Regex("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}").matches(microsoft.clientId!!)) { "Client id is not valid" }
    return microsoft
}

public suspend fun keycloakAuth(builder: suspend Keycloak.() -> Unit): Keycloak {
    val keycloak = Keycloak().apply { builder() }
    return keycloak
}
