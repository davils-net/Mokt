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

package dev.redtronics.mokt.html

import kotlinx.html.*

public fun HTML.redirectPage(
    textColor: String,
    firstAccentColor: String,
    secondAccentColor: String,
    title: String,
    notice: String
) {
    head {
        title(title)

        style {
            //language=CSS
            +"""
            body {
                display: flex;
                align-items: center;
                justify-content: center;
                height: 100vh;
                margin: 0;
                color: $textColor;
                font-family: 'Arial', sans-serif;
                overflow: hidden;
                position: relative;
                background: linear-gradient(45deg, $firstAccentColor, $secondAccentColor);
                background-size: 400% 400%;
                animation: gradientAnimation 20s infinite;
                text-align: center;
            }

            @keyframes gradientAnimation {
                0% {
                    background-position: 0 50%;
                }
                50% {
                    background-position: 100% 50%;
                }
                100% {
                    background-position: 0 50%;
                }

            .container {
                text-align: center;
                transform: translateY(-50px);
                animation: fadeInUp 1.5s ease-out forwards;
                z-index: 1;
            }

            h1 {
                font-size: 3em;
                font-weight: bold;
                animation: textSlideIn 2s ease-out forwards;
            }

            p {
                font-size: 1.2em;
                margin-top: 20px;
            }

            @keyframes fadeInUp {
                to {
                    transform: translateY(0);
                }
            }

            @keyframes textSlideIn {
                from {
                    transform: translateY(-20px);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }
            """.trimIndent()
        }
    }

    body {
        div("container") {
            h1 {
                text(title)
            }
            p {
                text(notice)
            }
        }
    }
}