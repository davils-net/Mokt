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

package dev.redtronics.mokt.builder.device

import dev.redtronics.mokt.MojangGameAuth
import dev.redtronics.mokt.Provider
import dev.redtronics.mokt.getEnv
import dev.redtronics.mokt.network.interval
import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.device.CodeErrorResponse
import dev.redtronics.mokt.response.device.DeviceAuthStateError
import dev.redtronics.mokt.response.device.DeviceAuthStateErrorItem
import dev.redtronics.mokt.response.device.DeviceCodeResponse
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.util.date.*
import kotlin.time.Duration.Companion.seconds

public abstract class DeviceAuth<out T : Provider> : MojangGameAuth<T>() {
    internal var codeServer: CIOApplicationEngine? = null

    public abstract val deviceCodeEndpointUrl: Url

    public abstract var grantType: String

    public var clientSecret: String? = getEnv("KEYCLOAK_CLIENT_SECRET")

    public suspend fun displayCode(deviceCodeResponse: DeviceCodeResponse, builder: suspend UserCodeBuilder.() -> Unit) {
        val userCodeBuilder = UserCodeBuilder(deviceCodeResponse).apply { builder() }
        codeServer = userCodeBuilder.build()
    }

    public suspend fun requestAuthorizationCode(
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (err: CodeErrorResponse) -> Unit = {},
    ): DeviceCodeResponse? {
        val response = provider.httpClient.submitForm(
            url = deviceCodeEndpointUrl.toString(),
            formParameters = parameters {
                append("client_id", provider.clientId!!)
                append("scope", provider.scopes.joinToString(" ") { it.value })

                if (clientSecret != null) {
                    append("client_secret", clientSecret!!)
                }

                additionalParameters.forEach {
                    append(it.key, it.value)
                }
            }
        )
        if (!response.status.isSuccess()) {
            onRequestError(provider.json.decodeFromString(CodeErrorResponse.serializer(), response.bodyAsText()))
            return null
        }
        return provider.json.decodeFromString(DeviceCodeResponse.serializer(), response.bodyAsText())
    }

    public suspend fun requestAccessToken(
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String> = mapOf(),
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit = {},
    ): AccessResponse? {
        val startTime = getTimeMillis()
        return authLoop(startTime, deviceCodeResponse, additionalParameters, onRequestError)
    }

    public suspend fun requestAccessToken(
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String> = mapOf(),
        displayUserCode: suspend UserCodeBuilder.() -> Unit = {},
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit = {},
    ): AccessResponse? {
        displayCode(deviceCodeResponse, displayUserCode)
        return requestAccessToken(deviceCodeResponse, additionalParameters, onRequestError)
    }

    internal suspend fun authLoop(
        startTime: Long,
        deviceCodeResponse: DeviceCodeResponse,
        additionalParameters: Map<String, String>,
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit,
    ): AccessResponse? = interval(
        interval = deviceCodeResponse.interval.seconds,
        cond = { getTimeMillis() - startTime < deviceCodeResponse.expiresIn * 1000 }
    ) {
        val response = provider.httpClient.submitForm(
            url = provider.tokenEndpointUrl.toString(),
            formParameters = parameters {
                append("client_id", provider.clientId!!)
                append("device_code", deviceCodeResponse.deviceCode)
                append("grant_type", grantType)
                additionalParameters.forEach {
                    append(it.key, it.value)
                }
            }
        )

        val responseBody = response.bodyAsText()
        if (responseBody.contains("error")) {
            val errorResponse = provider.json.decodeFromString(DeviceAuthStateError.serializer(), responseBody)
            if (errorResponse.error != DeviceAuthStateErrorItem.AUTHORIZATION_PENDING) {
                onRequestError(errorResponse)
                cancel()
            }
            return@interval null
        }

        codeServer?.stop()
        return@interval provider.json.decodeFromString(AccessResponse.serializer(), responseBody)
    }
}
