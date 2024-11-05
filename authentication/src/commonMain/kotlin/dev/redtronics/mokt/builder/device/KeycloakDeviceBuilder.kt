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

package dev.redtronics.mokt.builder.device

import dev.redtronics.mokt.Keycloak
import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.device.CodeErrorResponse
import dev.redtronics.mokt.response.device.DeviceAuthStateError
import dev.redtronics.mokt.response.device.DeviceCodeResponse
import io.ktor.http.*

public class KeycloakDeviceBuilder internal constructor(override val provider: Keycloak) : DeviceAuth<Keycloak>() {
    override val deviceCodeEndpointUrl: Url
        get() = Url("")

    override val grantType: String
        get() = TODO("Not yet implemented")

    override suspend fun requestAuthorizationCode(onRequestError: suspend (err: CodeErrorResponse) -> Unit): DeviceCodeResponse? {
        TODO("Not yet implemented")
    }

    override suspend fun requestAccessToken(
        deviceCodeResponse: DeviceCodeResponse,
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit
    ): AccessResponse? {
        TODO("Not yet implemented")
    }

    override suspend fun authLoop(
        startTime: Long,
        deviceCodeResponse: DeviceCodeResponse,
        onRequestError: suspend (err: DeviceAuthStateError) -> Unit
    ): AccessResponse? {
        TODO("Not yet implemented")
    }
}