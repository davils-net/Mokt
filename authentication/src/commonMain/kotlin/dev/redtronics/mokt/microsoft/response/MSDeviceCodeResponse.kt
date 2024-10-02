/*
 * MIT License
 * Copyright 2024 Nils Jäkel
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

package dev.redtronics.mokt.microsoft.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MSDeviceCodeResponse(
    @SerialName("device_code")
    public val deviceCode: String,
    @SerialName("user_code")
    public val userCode: String,
    @SerialName("verification_uri")
    public val verificationUri: String,
    @SerialName("expires_in")
    public val expiresIn: Int,
    public val interval: Int,
    public val message: String
)