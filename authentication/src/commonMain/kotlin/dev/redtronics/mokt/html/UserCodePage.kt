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

package dev.redtronics.mokt.html

import kotlinx.html.*

/**
 * Simple template page to display the user code.
 *
 * @param userCode The user code to be displayed.
 * @param theme The theme to be used.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public fun HTML.userCodePage(userCode: String, theme: WebTheme) {
    head {
        title("Device Code")
    }

    body {
        div("card") {
            img(
                alt = "Mokt full logo",
                src = "https://code.redtronics.dev/nils.jaekel/mokt/-/raw/master/assets/mokt_m_alpha.png?ref_type=heads",
                classes = "mokt"
            )
            div("code") {
                p { text("Enter the code below in your browser") }
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
                    background-image: url(https://code.redtronics.dev/nils.jaekel/mokt/-/raw/feat/grant-auth/assets/background.png);
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
 * Represents the theme of the user code page.
 * Dark or Light Mode
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public enum class WebTheme {
    LIGHT,
    DARK;
}