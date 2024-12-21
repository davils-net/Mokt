/*
 * MIT License
 * Copyright 2024 Davils
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.davils.mokt.builder.mojang

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import net.davils.mokt.flows.GameAuthData
import net.davils.mokt.payload.XBoxPayload
import net.davils.mokt.payload.XBoxProperties
import net.davils.mokt.response.mojang.XBoxResponse

/**
 * Configures the auth on the microsoft store servers to get the xbox token access response.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class XBoxBuilder internal constructor(
    override val httpClient: HttpClient,
    override val json: Json,

    /**
     * The microsoft access token.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private val accessToken: String
) : GameAuthBuilder() {
    /**
     * The relying party url.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val relyingPartyUrl: Url
        get() = Url("http://auth.xboxlive.com")

    /**
     * The xbox login endpoint. `https://user.auth.xboxlive.com/user/authenticate`
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val xboxLoginEndpoint: Url
        get() = Url("https://user.auth.xboxlive.com/user/authenticate")

    /**
     * The token type. It is always [TokenType.JWT].
     * Other values are not supported by the xbox api to get the token.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val tokenType: TokenType = TokenType.JWT

    /**
     * The auth method. It is always `RPS`.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val authMethod: String
        get() = "RPS"

    /**
     * The rps ticket with access token.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val rpsTicket: String
        get() = "d=${accessToken}"

    /**
     * Returns the XBox access token response.
     *
     * @param onRequestError The function to be called if an error occurs during the XBox access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun build(onRequestError: suspend (response: HttpResponse) -> Unit): XBoxResponse? {
        val response = requestAuthData()
        if (!response.status.isSuccess()) {
            onRequestError(response)
            return null
        }

        return json.decodeFromString(XBoxResponse.serializer(), response.bodyAsText())
    }

    /**
     * Executes the XBox access token response.
     *
     * @param flowData The flow data.
     * @param onRequestError The function to be called if an error occurs during the XBox access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun <T : GameAuthData> build(flowData: T, onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit): XBoxResponse? {
        val response = requestAuthData()
        if (!response.status.isSuccess()) {
            onRequestError(response, flowData)
            return null
        }

        return json.decodeFromString(XBoxResponse.serializer(), response.bodyAsText())
    }

    /**
     * Executes the XBox access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override suspend fun requestAuthData(): HttpResponse {
        val payload = XBoxPayload(
            properties = XBoxProperties(
                authMethod = authMethod,
                siteName = xboxLoginEndpoint.host,
                rpsTicket = rpsTicket
            ),
            relyingParty = relyingPartyUrl.toString(),
            tokenType = tokenType
        )

        val response = httpClient.post {
            url(xboxLoginEndpoint)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(XBoxPayload.serializer(), payload))
        }
        return response
    }
}

/**
 * Represents the token type.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable(with = TokenType.Serializer::class)
public enum class TokenType(public val type: String) {
    JWT("JWT"),
    UNKNOWN("UNKNOWN");

    /**
     * Custom serializer for [TokenType].
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal object Serializer : KSerializer<TokenType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(TokenType::class.simpleName!!, PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): TokenType = entries.find { it.type == decoder.decodeString() } ?: UNKNOWN
        override fun serialize(encoder: Encoder, value: TokenType) {
            encoder.encodeString(value.type)
        }
    }
}
