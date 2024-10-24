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

@Serializable
internal data class XBoxPayload(
    @SerialName("Properties")
    val properties: XBoxProperties,
    val relyingParty: String,
    val tokenType: TokenType
)

@Serializable
internal data class XBoxProperties(
    @SerialName("AuthMethod")
    val xAuthMethod: String,
    @SerialName("SiteName")
    val siteName: String,
    @SerialName("RpsTicket")
    val rpsTicket: String
)