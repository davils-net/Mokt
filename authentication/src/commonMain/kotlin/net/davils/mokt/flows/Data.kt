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

package net.davils.mokt.flows

import kotlinx.serialization.Serializable
import net.davils.mokt.response.AccessResponse
import net.davils.mokt.response.GrantCodeResponse
import net.davils.mokt.response.device.DeviceCodeResponse
import net.davils.mokt.response.mojang.MojangResponse
import net.davils.mokt.response.mojang.XBoxResponse
import net.davils.mokt.response.mojang.XstsResponse

/**
 * The base data, which is used for all flows.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public sealed class AuthData : FlowData() {
    /**
     * The generic access response from any provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public abstract var accessResponse: AccessResponse?
}

/**
 * Flow data for the grant authentication.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public abstract class GrantAuthData(
    /**
     * The access response from the grant authentication.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override var accessResponse: AccessResponse? = null,

    /**
     * The grant code response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var grantCodeResponse: GrantCodeResponse? = null
) : AuthData()

/**
 * Flow data for the device authentication.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public abstract class DeviceAuthData(
    /**
     * The access response from the device authentication.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override var accessResponse: AccessResponse? = null,

    /**
     * The device code response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var deviceCodeResponse: DeviceCodeResponse? = null
) : AuthData()

/**
 * The Keycloak authentication data.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public abstract class KeycloakAuthData : AuthData() {
    /**
     * The keycloak access response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override var accessResponse: AccessResponse? = null

    /**
     * The requested microsoft access response from keycloak provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var microsoftAccessResponse: AccessResponse? = null
}

/**
 * The flow data for the game authentication.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public abstract class GameAuthData(
    /**
     * The xbox response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var xBoxResponse: XBoxResponse? = null,

    /**
     * The xsts response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var xstsResponse: XstsResponse? = null,

    /**
     * The mojang response.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var mojangResponse: MojangResponse? = null
) : FlowData()

/**
 * The calculated flow progress from the authentication.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public data class AuthProgress<out T : AuthState>(
    override val currentStep: Int,
    override val totalSteps: Int,

    /**
     * The current auth state.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val state: T
) : FlowProgress()
