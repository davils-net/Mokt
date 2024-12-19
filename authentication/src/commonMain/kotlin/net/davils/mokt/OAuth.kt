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

package net.davils.mokt

/**
 * Base interface for all authentication flows.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public abstract class OAuth<out T : Provider> : GameAuth<T>() {
    /**
     * OAuth 2.0 Grant Type
     *
     * A grant type is the method by which an application requests an access token.
     * Each grant type is optimized for a specific use case, such as web apps, native apps, devices
     * without a web browser, or server-to-server applications.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     */
    public abstract val grantType: GrantType
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

/**
 * OAuth 2.0 Grant Type
 *
 * A grant type is the method by which an application requests an access token.
 * Each grant type is optimized for a specific use case, such as web apps, native apps, devices
 * without a web browser, or server-to-server applications.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public enum class GrantType(public val value: String) {
    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token"),
    DEVICE_CODE("urn:ietf:params:oauth:grant-type:device_code");
}
