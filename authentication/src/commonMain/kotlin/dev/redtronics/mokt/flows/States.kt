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

public enum class DeviceAuthState(override val description: String) : AuthState {
    REQUEST_DEVICE_CODE("Request device code"),
    DISPLAY_USER_CODE("Display user code"),
    AUTHORIZE_PENDING_USER_CODE("Authorize pending user code"),
    REQUEST_ACCESS_TOKEN_FROM_AUTH_PROVIDER("Request access token from auth provider"),
    REQUEST_ACCESS_TOKEN_FROM_MICROSOFT("Request access token from microsoft"),
    REQUEST_REFRESH_TOKEN_FROM_MICROSOFT("Request refresh token from microsoft");
}

public enum class GrantAuthState(override val description: String) : AuthState {
    REQUEST_GRANT_CODE("Request auth code"),
    REQUEST_ACCESS_TOKEN_FROM_AUTH_PROVIDER("Request access token from auth provider"),
    REQUEST_ACCESS_TOKEN_FROM_MICROSOFT("Request access token from microsoft"),
    REQUEST_REFRESH_TOKEN_FROM_MICROSOFT("Request refresh token from microsoft");
}