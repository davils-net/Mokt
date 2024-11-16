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

package dev.redtronics.mokt.flows

public interface AuthState {
    public val description: String
}

public enum class OAuthState(override val description: String) : AuthState {
    REQUEST_DEVICE_CODE("Request device code"),
    REQUEST_GRANT_CODE("Request grant code"),
    DISPLAY_USER_CODE("Display user code"),
    AUTHORIZE_PENDING_USER_CODE("Authorize pending user code"),
    REQUEST_ACCESS_TOKEN("Request access token from microsoft"),
    REQUEST_REFRESH_TOKEN("Request refresh token from microsoft");
}

public enum class MinecraftAuthState(override val description: String) : AuthState {
    REQUEST_XBOX_ACCESS_TOKEN("Request XBox access token"),
    REQUEST_XSTS_ACCESS_TOKEN("Request xsts access token"),
    REQUEST_MOJANG_TOKEN("Request mojang access token");
}
