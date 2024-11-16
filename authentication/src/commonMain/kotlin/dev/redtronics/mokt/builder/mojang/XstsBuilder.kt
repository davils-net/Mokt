/*
 * MIT License
 * Copyright 2024 Nils Jäkel & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package dev.redtronics.mokt.builder.mojang

import dev.redtronics.mokt.flows.GameAuthData
import dev.redtronics.mokt.payload.XstsPayload
import dev.redtronics.mokt.payload.XstsProperties
import dev.redtronics.mokt.response.mojang.XBoxResponse
import dev.redtronics.mokt.response.mojang.XstsResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Configures the auth on the microsoft store servers to get the xsts token access response.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class XstsBuilder internal constructor(
    override val httpClient: HttpClient,
    override val json: Json,

    /**
     * The XBox access token response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private val xBoxResponse: XBoxResponse
) : GameAuthBuilder() {
    /**
     * The sandbox id of the payload.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val sandboxId: String
        get() = "RETAIL"

    /**
     * The relying party, depending on the platform you would authenticate on. ([RelyingParty.JAVA], [RelyingParty.BEDROCK])
     * If you authenticate with bedrock you can use the xsts token directly for the bedrock servers.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var relyingParty: RelyingParty = RelyingParty.JAVA

    /**
     * The auth endpoint to get the xsts token access response. `https://xsts.auth.xboxlive.com/xsts/authorize`
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val xstsLoginEndpoint: Url
        get() = Url("https://xsts.auth.xboxlive.com/xsts/authorize")

    /**
     * Executes the xsts token access response.
     *
     * @param onRequestError The function to be called if an error occurs during the xsts access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun build(onRequestError: suspend (response: HttpResponse) -> Unit): XstsResponse?  {
        val response = requestAuthData()
        if (!response.status.isSuccess()) {
            onRequestError(response)
            return null
        }

        return json.decodeFromString(XstsResponse.serializer(), response.bodyAsText())
    }

    /**
     * Executes the xsts token access response.
     *
     * @param onRequestError The function to be called if an error occurs during the xsts access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun <T : GameAuthData> build(flowData: T, onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit): XstsResponse?  {
        val response = requestAuthData()
        if (!response.status.isSuccess()) {
            onRequestError(response, flowData)
            return null
        }

        return json.decodeFromString(XstsResponse.serializer(), response.bodyAsText())
    }

    /**
     * Executes the xsts token access response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override suspend fun requestAuthData(): HttpResponse {
        val xstsPayload = XstsPayload(
            properties = XstsProperties(
                sandboxId = sandboxId,
                userTokens = listOf(xBoxResponse.token)
            ),
            relyingParty = relyingParty.party,
            tokenType = TokenType.JWT
        )

        val response = httpClient.post {
            url(xstsLoginEndpoint)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(XstsPayload.serializer(), xstsPayload))
        }
        return response
    }
}

/**
 * The relying party, depending on the platform you would authenticate on. ([RelyingParty.JAVA], [RelyingParty.BEDROCK])
 * If you authenticate with bedrock you can use the xsts token directly for the bedrock servers.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public enum class RelyingParty(public val party: String) {
    JAVA("rp://api.minecraftservices.com/"),
    BEDROCK("https://pocket.realms.minecraft.net/");
}