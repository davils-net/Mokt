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

package dev.redtronics.mokt

import dev.redtronics.mokt.builder.device.KeycloakDeviceBuilder
import dev.redtronics.mokt.network.client
import dev.redtronics.mokt.network.defaultJson
import dev.redtronics.mokt.network.div
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

public class Keycloak internal constructor() : Provider() {
    override val name: String
        get() = "Keycloak"

    override var httpClient: HttpClient = client

    override var json: Json = defaultJson

    override var clientId: String? = getEnv("KEYCLOAK_CLIENT_ID")

    public var instanceUrl: Url? = null

    override val tokenEndpointUrl: Url
        get() = instanceUrl!! / "/realms/$realm/protocol/openid-connect/token"

    public var realm: String? = null

    public suspend fun <T> device(builder: suspend KeycloakDeviceBuilder.() -> T): T {
        val keycloakBuilder = KeycloakDeviceBuilder(this)
        return builder(keycloakBuilder).apply { build() }
    }

    override suspend fun build() {
        require(clientId != null && realm != null && instanceUrl != null) { "You need to set client id, realm and instance url" }
    }
}
