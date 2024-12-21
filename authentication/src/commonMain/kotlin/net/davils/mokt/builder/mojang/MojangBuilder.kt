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
import kotlinx.serialization.json.Json
import net.davils.mokt.flows.GameAuthData
import net.davils.mokt.payload.MojangPayload
import net.davils.mokt.response.mojang.MojangResponse
import net.davils.mokt.response.mojang.XstsResponse

/**
 * Configuration for the Mojang authentication to get the Minecraft access token.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class MojangBuilder internal constructor(
    override val httpClient: HttpClient,
    override val json: Json,

    /**
     * The xsts access token response. (Microsoft store services)
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    private val xstsResponse: XstsResponse
) : GameAuthBuilder() {
    /**
     * The login endpoint of the Mojang API. `https://api.minecraftservices.com/authentication/login_with_xbox`
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val minecraftLoginEndpoint: Url
        get() = Url("https://api.minecraftservices.com/authentication/login_with_xbox")

    /**
     * Executes the mojang access token request.
     *
     * @param onRequestError The function to be called if an error occurs during the mojang access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun build(onRequestError: suspend (response: HttpResponse) -> Unit): MojangResponse? {
        val response = requestAuthData()
        if (!response.status.isSuccess()) {
            onRequestError(response)
            return null
        }

        return json.decodeFromString(MojangResponse.serializer(), response.bodyAsText())
    }

    /**
     * Executes the mojang access token request.
     *
     * @param onRequestError The function to be called if an error occurs during the mojang access token request.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal suspend fun <T : GameAuthData> build(flowData: T, onRequestError: suspend (response: HttpResponse, flowData: T) -> Unit): MojangResponse? {
        val response = requestAuthData()
        if (!response.status.isSuccess()) {
            onRequestError(response, flowData)
            return null
        }

        return json.decodeFromString(MojangResponse.serializer(), response.bodyAsText())
    }

    /**
     * Requests the mojang access token from the login endpoint.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override suspend fun requestAuthData(): HttpResponse {
        val payload = MojangPayload(
            identityToken = "XBL3.0 x=${xstsResponse.displayClaims.xui[0].uhs};${xstsResponse.token}"
        )

        val response = httpClient.post {
            url(minecraftLoginEndpoint)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(MojangPayload.serializer(), payload))
        }
        return response
    }
}
