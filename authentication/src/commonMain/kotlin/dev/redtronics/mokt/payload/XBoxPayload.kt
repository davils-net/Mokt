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

package dev.redtronics.mokt.payload

import dev.redtronics.mokt.builder.mojang.TokenType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the payload for the XSTS (Xbox Secure Token Service) endpoint.
 *
 * @property properties The properties of the payload.
 * @property relyingParty The relying party of the payload.
 * @property tokenType The type of the payload.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
internal data class XBoxPayload(
    @SerialName("Properties")
    val properties: XBoxProperties,
    val relyingParty: String,
    val tokenType: TokenType
)

/**
 * Represents the properties of the xbox payload.
 *
 * @property xAuthMethod The auth method of the payload.
 * @property siteName The site name of the payload.
 * @property rpsTicket The rps ticket of the payload.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
internal data class XBoxProperties(
    @SerialName("AuthMethod")
    val xAuthMethod: String,
    @SerialName("SiteName")
    val siteName: String,
    @SerialName("RpsTicket")
    val rpsTicket: String
)
