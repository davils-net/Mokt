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

package dev.redtronics.mokt.response.mojang

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response from the Mojang API.
 *
 * @property username The username of the user.
 * @property roles The roles of the user.
 * @property accessToken The access token of the user.
 * @property tokenType The type of the access token.
 * @property expiresIn The expiration time of the access token.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
@Serializable
public data class MojangResponse(
    public val username: String,
    public val roles: List<String>,
    @SerialName("access_token")
    public val accessToken: String,
    @SerialName("token_type")
    public val tokenType: String,
    @SerialName("expires_in")
    public val expiresIn: Int
)
