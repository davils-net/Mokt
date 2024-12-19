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

package net.davils.mokt.builder.device

import io.ktor.http.*
import net.davils.mokt.Keycloak
import net.davils.mokt.network.div

/**
 * Keycloak device authentication builder.
 * Configures the device authentication flow for the Keycloak provider.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class KeycloakDeviceBuilder internal constructor(override val provider: Keycloak) : DeviceAuth<Keycloak>() {
    /**
     * The url of the device authentication endpoint to get the device and user code.
     * Also, the url would automatically resolve to the correct realm.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val deviceCodeEndpointUrl: Url
        get() = provider.instanceUrl / "/realms/${provider.realm}/protocol/openid-connect/auth/device"
}