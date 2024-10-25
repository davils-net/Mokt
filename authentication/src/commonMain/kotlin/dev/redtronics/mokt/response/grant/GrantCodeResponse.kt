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

package dev.redtronics.mokt.response.grant

import kotlinx.serialization.Serializable

/**
 * Represents the OAuth Code from the auth endpoints.
 *
 * @property code The OAuth Code.
 * @property state The state of the code as [Int].
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public data class GrantCodeResponse(
    public val code: String,
    public val state: String
)
