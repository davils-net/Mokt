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

package net.davils.mokt.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the access token response from the oauth2.0 standard.
 *
 * @property tokenType The type of the access token.
 * @property scope The scope of the access token.
 * @property expiresIn The expiration time of the access token.
 * @property extExpiresIn The expiration time of the access token.
 * @property accessToken The access token.
 * @property refreshToken The refresh token.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public data class AccessResponse(
    @SerialName("token_type")
    public val tokenType: String,
    public val scope: String,
    @SerialName("expires_in")
    public val expiresIn: Int,
    @SerialName("ext_expires_in")
    public val extExpiresIn: Int? = null,
    @SerialName("access_token")
    public val accessToken: String,
    @SerialName("refresh_token")
    public val refreshToken: String,
    @SerialName("id_token")
    public val idToken: String? = null
)