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

/**
 * Keycloak grant code builder.
 * Configures the code grant flow for the Keycloak provider.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class KeycloakGrantBuilder internal constructor(
    /**
     * The Keycloak provider instance.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val provider: Keycloak
) : GrantAuth<Keycloak>() {
    /**
     * The authorization endpoint URL for the Keycloak provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val authorizeEndpointUrl: Url
        get() = provider.instanceUrl / "/realms/${provider.realm}/protocol/openid-connect/auth"
}