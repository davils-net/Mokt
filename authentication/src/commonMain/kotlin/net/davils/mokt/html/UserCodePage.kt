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

package net.davils.mokt.html

import io.ktor.http.*
import kotlinx.html.*

/**
 * Simple template page to display the user code.
 *
 * @param userCode The user code to be displayed.
 * @param title The title of the page.
 * @param logoUrl The url of the logo.
 * @param logoDescription The description of the logo.
 * @param backgroundUrl The url of the background image.
 * @param userCodeHint The hint for the user code.
 * @param theme The theme of the page.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public fun HTML.userCodePage(
    userCode: String,
    title: String,
    logoUrl: Url,
    logoDescription: String,
    backgroundUrl: Url,
    userCodeHint: String,
    theme: WebTheme
) {
    head {
        title(title)
    }

    body {
        div("card") {
            img(
                alt = logoDescription,
                src = logoUrl.toString(),
                classes = "mokt"
            )
            div("code") {
                p { text(userCodeHint) }
                h1 { text(userCode) }
            }
        }

        div("credits") {
            p { text("Created with Mokt") }
        }

        style {
            unsafe {
                // language=CSS
                +"""
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }

                body {
                    background-image: url("$backgroundUrl");
                    background-position: center;
                    background-size: cover;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 100vh;
                }

                .card {
                    display: flex;
                    width: 390px;
                    height: 450px;
                    align-items: center;
                    justify-content: center;
                    flex-direction: column;
                    padding: 20px;
                    background-color: ${if (theme == WebTheme.LIGHT) "#ffffff" else "#2d2d2d"};
                }

                .card img {
                    width: 30%;
                    height: 30%;
                    margin-bottom: 34px;
                }

                .code {
                    display: flex;
                    flex-direction: column;
                    width: 100%;
                    padding: 12px;
                    text-align: center;
                    color: ${if (theme == WebTheme.LIGHT) "#000000" else "#ffffff"}
                }

                .credits {
                    position: absolute;
                    bottom: 50px;
                    color: #ffffff;
                    font-size: 15px;
                }
            """.trimIndent()
            }
        }
    }
}

/**
 * Represents the theme of the user code page. (Dark/Light)
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public enum class WebTheme {
    LIGHT,
    DARK;
}