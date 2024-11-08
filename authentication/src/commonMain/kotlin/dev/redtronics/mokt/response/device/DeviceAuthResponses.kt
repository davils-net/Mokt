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

package dev.redtronics.mokt.response.device

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the Device Code Response from the auth endpoints.
 *
 * @property deviceCode The Device Code.
 * @property userCode The User Code.
 * @property verificationUri The Verification URI.
 * @property expiresIn The Expiration Time.
 * @property interval The Interval to ping the verification URI.
 * @property message The Message.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public data class DeviceCodeResponse(
    @SerialName("device_code")
    public val deviceCode: String,
    @SerialName("user_code")
    public val userCode: String,
    @SerialName("verification_uri")
    public val verificationUri: String,
    @SerialName("verification_uri_complete")
    public val verificationUriComplete: String? = null,
    @SerialName("expires_in")
    public val expiresIn: Int,
    public val interval: Int,
    public val message: String? = null
)
