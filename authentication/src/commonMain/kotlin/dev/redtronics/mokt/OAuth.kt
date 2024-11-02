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

@file:Suppress("MemberVisibilityCanBePrivate")

package dev.redtronics.mokt

/**
 * Defines the different authentication methods used by the oauth.
 *
 * @property authMethodName The name of the authentication method.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public enum class AuthMethod(public val authMethodName: String) {
    /**
     * The grant code authentication method.
     * */
    GRANT_CODE("grant_code"),

    /**
     * The device authentication method.
     * */
    DEVICE_AUTH("device_auth");
}
