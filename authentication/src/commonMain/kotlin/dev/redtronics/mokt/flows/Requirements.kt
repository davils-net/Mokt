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

package dev.redtronics.mokt.flows

import dev.redtronics.mokt.response.AccessResponse
import dev.redtronics.mokt.response.GrantCodeResponse
import dev.redtronics.mokt.response.device.DeviceCodeResponse
import kotlinx.serialization.Serializable

/**
 * The base data, which is used for all flows.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public sealed interface AuthData {
    public val accessResponse: AccessResponse?
}

@Serializable
public abstract class GrantAuthData(
    override var accessResponse: AccessResponse? = null,
    public var grantCodeResponse: GrantCodeResponse? = null
) : FlowData(), AuthData

@Serializable
public abstract class DeviceAuthData(
    override var accessResponse: AccessResponse? = null,
    public var deviceCodeResponse: DeviceCodeResponse? = null
) : FlowData(), AuthData

@Serializable
public data class AuthProgress<out T : AuthState>(
    override val currentStep: Int,
    override val totalSteps: Int,
    public val state: T
) : FlowProgress()
