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
import dev.redtronics.mokt.builder.mojang.RelyingParty
import dev.redtronics.mokt.builder.mojang.XBoxBuilder
import dev.redtronics.mokt.builder.mojang.XstsBuilder
import dev.redtronics.mokt.response.mojang.MojangResponse
import dev.redtronics.mokt.response.mojang.XBoxResponse
import dev.redtronics.mokt.response.mojang.XstsResponse
import io.ktor.client.statement.*

/**
 * Implements the complete mojang authentication to the provider, that is inherited from [MojangGameAuth].
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public abstract class MojangGameAuth<out T : Provider> internal constructor() {
    /**
     * The provider that is inherited from [MojangGameAuth].
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal abstract val provider: T

    /**
     * Interacts with the XBox endpoint to get the XBox access token and their response.
     *
     * @param accessToken The microsoft access token.
     * @param onRequestError The function to be called if an error occurs during the XBox access token request.
     * @param builder The builder to configure the XBox access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun xBox(
        accessToken: String,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
        builder: suspend XBoxBuilder.() -> Unit = {}
    ): XBoxResponse? {
        val xBoxBuilder = XBoxBuilder(provider.httpClient, provider.json, accessToken).apply { builder() }
        return xBoxBuilder.build(onRequestError)
    }

    /**
     * Interacts with the microsoft store endpoint to get the xsts access token and their response.
     *
     * @param xBoxResponse The XBox access token response.
     * @param onRequestError The function to be called if an error occurs during the xsts access token request.
     * @param builder The builder to configure the xsts access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun xsts(
        xBoxResponse: XBoxResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
        builder: suspend XstsBuilder.() -> Unit
    ): XstsResponse? {
        val xstsBuilder = XstsBuilder(provider.httpClient, provider.json, xBoxResponse).apply { builder() }
        return xstsBuilder.build(onRequestError)
    }

    /**
     * Interacts with the microsoft store endpoint to get the xsts access token and their response.
     *
     * @param xBoxResponse The XBox access token response.
     * @param onRequestError The function to be called if an error occurs during the xsts access token request.
     * @param relyingParty The relying party of the xsts access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun xsts(
        xBoxResponse: XBoxResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
        relyingParty: RelyingParty = RelyingParty.JAVA
    ): XstsResponse? {
        val xsts = xsts(xBoxResponse, onRequestError) {
            this.relyingParty = relyingParty
        }
        return xsts
    }

    /**
     * Interacts with the mojang endpoint to get the mojang access token and their response.
     *
     * @param xstsResponse The xsts access token response.
     * @param onRequestError The function to be called if an error occurs during the mojang access token request.
     * @param builder The builder to configure the mojang access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun mojang(
        xstsResponse: XstsResponse,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
        builder: suspend MojangBuilder.() -> Unit = {}
    ): MojangResponse? {
        val mojangBuilder = MojangBuilder(provider.httpClient, provider.json, xstsResponse).apply { builder() }
        return mojangBuilder.build(onRequestError)
    }
}