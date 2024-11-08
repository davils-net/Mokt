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

package dev.redtronics.mokt

/**
 * Base interface for all authentication flows.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public interface OAuth {
    /**
     * OAuth 2.0 Grant Type
     *
     * A grant type is the method by which an application requests an access token.
     * OAuth 2.0 defines several standard grant types, including:
     *
     * * Authorization Code Flow: Used for web applications.
     * * Client Credentials Flow: Used for server-to-server communication.
     * * Refresh Token Flow: Used for obtaining a new access token using a refresh token.
     * * Password Flow: Used for obtaining an access token using a username and password.
     *
     * Each grant type is optimized for a specific use case, such as web apps, native apps, devices without a web browser, or server-to-server applications.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     */
    public val grantType: String
}

/**
 * The [Scope] in the request path can be set to specify which resources or permissions your
 * application is requesting access to.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public enum class Scope(public val value: String) {
    OPENID("openid"),
    PROFILE("profile"),
    EMAIL("email"),
    OFFLINE_ACCESS("offline_access"),
    XBOX_LIVE_SIGNIN("XBoxLive.signin");

    public companion object {
        /**
         * A list of all available scopes.
         *
         * @since 0.0.1
         * @author Nils Jäkel
         * */
        public val allScopes: List<Scope>
            get() = entries.toList()
    }
}