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

import dev.redtronics.mokt.builder.TokenType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class XstsPayload(
    @SerialName("Properties")
    val properties: XstsProperties,
    @SerialName("RelyingParty")
    val relyingParty: String,
    @SerialName("TokenType")
    val tokenType: TokenType
)

@Serializable
internal data class XstsProperties(
    @SerialName("SandboxId")
    val sandboxId: String,
    @SerialName("UserTokens")
    val userTokens: List<String>
)