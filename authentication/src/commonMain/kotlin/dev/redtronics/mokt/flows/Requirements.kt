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

public abstract class GrantAuthData(
    public var grantCodeResponse: GrantCodeResponse? = null,
    public var accessResponse: AccessResponse? = null
) : FlowData()

public data class AuthProgress<out T : AuthState>(
    override val currentStep: Int,
    override val totalSteps: Int,
    public val state: T
) : FlowProgress()
