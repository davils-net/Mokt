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

package net.davils.mokt.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.davils.mokt.builder.mojang.TokenType

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
internal data class XstsPayload(
    @SerialName("Properties")
    val properties: XstsProperties,
    @SerialName("RelyingParty")
    val relyingParty: String,
    @SerialName("TokenType")
    val tokenType: TokenType
)

/**
 * Represents the properties of the xsts payload.
 *
 * @property sandboxId The sandbox id of the payload.
 * @property userTokens The user tokens of the payload.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
internal data class XstsProperties(
    @SerialName("SandboxId")
    val sandboxId: String,
    @SerialName("UserTokens")
    val userTokens: List<String>
)