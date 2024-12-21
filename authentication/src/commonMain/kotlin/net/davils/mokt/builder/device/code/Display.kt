/*
 * MIT License
 * Copyright 2024 Davils
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

package net.davils.mokt.builder.device.code

/**
 * Base class for displaying the user code.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public sealed class Display {
    /**
     * The title for the displaying method.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var title: String = "Device Code"

    /**
     * Hints for the user, what to do next.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var userCodeHint: String = "Enter the code below in your browser"
}