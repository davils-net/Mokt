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

package net.davils.mokt

import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import net.davils.mokt.builder.mojang.MojangBuilder
import net.davils.mokt.builder.mojang.RelyingParty
import net.davils.mokt.builder.mojang.XBoxBuilder
import net.davils.mokt.builder.mojang.XstsBuilder
import net.davils.mokt.flows.AuthProgress
import net.davils.mokt.flows.FlowStep
import net.davils.mokt.flows.GameAuthData
import net.davils.mokt.flows.GameAuthState
import net.davils.mokt.response.mojang.MojangResponse
import net.davils.mokt.response.mojang.XBoxResponse
import net.davils.mokt.response.mojang.XstsResponse

/**
 * Implements the complete mojang authentication to the provider, that is inherited from [GameAuth].
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public abstract class GameAuth<out T : Provider> internal constructor() {
    /**
     * The provider that is inherited from [GameAuth].
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
        builder: suspend XBoxBuilder.() -> Unit = {},
    ): XBoxResponse? {
        val xBoxBuilder = XBoxBuilder(provider.httpClient, provider.json, accessToken).apply { builder() }
        return xBoxBuilder.build(onRequestError)
    }

    /**
     * Interacts with the XBox endpoint to get the XBox access token.
     *
     * @param accessToken The microsoft access token.
     * @param onRequestError The function to be called if an error occurs during the XBox access token request.
     * @param builder The builder to configure the XBox access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : GameAuthData> xBox(
        accessToken: String,
        onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
        builder: suspend XBoxBuilder.() -> Unit = {},
    ): FlowStep<T, AuthProgress<GameAuthState>> = object : FlowStep<T, AuthProgress<GameAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<GameAuthState>> = channelFlow {
            send(AuthProgress(1, 2, GameAuthState.REQUEST_XBOX_TOKEN))

            val xBoxBuilder = XBoxBuilder(provider.httpClient, provider.json, accessToken).apply { builder() }
            val response = xBoxBuilder.build(flowData, onRequestError)
            flowData.xBoxResponse = response
            send(AuthProgress(2, 2, GameAuthState.REQUEST_XBOX_TOKEN))
        }
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
        builder: suspend XstsBuilder.() -> Unit,
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
        relyingParty: RelyingParty = RelyingParty.JAVA,
        onRequestError: suspend (response: HttpResponse) -> Unit = {},
    ): XstsResponse? {
        val xsts = xsts(xBoxResponse, onRequestError) {
            this.relyingParty = relyingParty
        }
        return xsts
    }

    /**
     * Interacts with the microsoft store endpoint to get the xsts access token.
     *
     * @param onRequestError The function to be called if an error occurs during the xsts access token request.
     * @param builder The builder to configure the xsts access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : GameAuthData> xsts(
        onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
        builder: suspend XstsBuilder.() -> Unit = {},
    ): FlowStep<T, AuthProgress<GameAuthState>> = object : FlowStep<T, AuthProgress<GameAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<GameAuthState>> = channelFlow {
            send(AuthProgress(1, 2, GameAuthState.REQUEST_XSTS_TOKEN))
            if (flowData.xBoxResponse == null) {
                send(AuthProgress(2, 2, GameAuthState.REQUEST_XSTS_TOKEN))
                return@channelFlow
            }

            val xstsBuilder = XstsBuilder(provider.httpClient, provider.json, flowData.xBoxResponse!!).apply { builder() }
            val response = xstsBuilder.build(flowData, onRequestError)

            flowData.xstsResponse = response
            send(AuthProgress(2, 2, GameAuthState.REQUEST_XSTS_TOKEN))
        }
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
        builder: suspend MojangBuilder.() -> Unit = {},
    ): MojangResponse? {
        val mojangBuilder = MojangBuilder(provider.httpClient, provider.json, xstsResponse).apply { builder() }
        return mojangBuilder.build(onRequestError)
    }

    /**
     * Interacts with the mojang endpoint to get the mojang access token.
     *
     * @param onRequestError The function to be called if an error occurs during the mojang access token request.
     * @param builder The builder to configure the mojang access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public fun <T : GameAuthData> mojang(
        onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit = { _, flowData -> flowData.cancel() },
        builder: suspend MojangBuilder.() -> Unit = {},
    ): FlowStep<T, AuthProgress<GameAuthState>> = object : FlowStep<T, AuthProgress<GameAuthState>> {
        override suspend fun execute(flowData: T): Flow<AuthProgress<GameAuthState>> = channelFlow {
            send(AuthProgress(1, 2, GameAuthState.REQUEST_MOJANG_TOKEN))
            if (flowData.xstsResponse == null) {
                send(AuthProgress(2, 2, GameAuthState.REQUEST_MOJANG_TOKEN))
                return@channelFlow
            }

            val mojangBuilder = MojangBuilder(provider.httpClient, provider.json, flowData.xstsResponse!!).apply { builder() }
            val response = mojangBuilder.build(flowData, onRequestError)

            flowData.mojangResponse = response
            send(AuthProgress(2, 2, GameAuthState.REQUEST_MOJANG_TOKEN))
        }
    }
}
