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

package net.davils.mokt.flows

/**
 * Base interface for all authentication states.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public interface AuthState {
    /**
     * The description of the state.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public val description: String
}

/**
 * Contains all oauth states.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public enum class OAuthState(override val description: String) : AuthState {
    REQUEST_USER_CODE("Request device code"),
    REQUEST_GRANT_CODE("Request grant code"),
    DISPLAY_USER_CODE("Display user code"),
    AUTHORIZE_PENDING_USER_CODE("Authorize pending user code"),
    REQUEST_ACCESS_TOKEN("Request access token from microsoft"),
    REQUEST_REFRESH_TOKEN("Request refresh token from microsoft");
}

/**
 * Contains all game auth states.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public enum class GameAuthState(override val description: String) : AuthState {
    REQUEST_XBOX_TOKEN("Request XBox access token"),
    REQUEST_XSTS_TOKEN("Request xsts access token"),
    REQUEST_MOJANG_TOKEN("Request mojang access token");
}
