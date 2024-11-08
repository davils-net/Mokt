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

package dev.redtronics.mokt.builder.grant

import dev.redtronics.mokt.Keycloak
import dev.redtronics.mokt.network.div
import io.ktor.http.*

public class KeycloakGrantBuilder internal constructor(override val provider: Keycloak) : GrantAuth<Keycloak>() {
    override val authorizeEndpointUrl: Url
        get() = provider.instanceUrl / "/realms/${provider.realm}/protocol/openid-connect/auth"
}