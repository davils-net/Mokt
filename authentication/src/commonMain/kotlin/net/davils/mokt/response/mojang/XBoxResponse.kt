/*
 * MIT License
 * Copyright 2024 Nils Jäkel & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

package net.davils.mokt.response.mojang

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the response from the Xbox Live service.
 *
 * @property issueInstant The time at which the token was issued.
 * @property notAfter The expiration time of the token.
 * @property token The security token provided by the Xbox Live service.
 * @property displayClaims The display claims provided by the Xbox Live service.
 *
 * @see DisplayClaims
 * @see Instant
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
@Serializable
public data class XBoxResponse(
    @SerialName("IssueInstant")
    public val issueInstant: Instant,
    @SerialName("NotAfter")
    public val notAfter: Instant,
    @SerialName("Token")
    public val token: String,
    @SerialName("DisplayClaims")
    public val displayClaims: DisplayClaims
)

