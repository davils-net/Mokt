/*
 * MIT License
 * Copyright 2024 Nils Jäkel  & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

package net.davils.mokt.html

import kotlinx.html.*
import net.davils.mokt.html.style.Color

/**
 * Simple page to display information as website on redirect.
 *
 * @param title The title of the page.
 * @param notice The notice to be displayed.
 * @param textColor The text color to be used.
 * @param firstAccentColor The first accent color to be used.
 * @param secondAccentColor The second accent color to be used.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public fun HTML.redirectPage(
    title: String,
    notice: String,
    textColor: Color,
    firstAccentColor: Color,
    secondAccentColor: Color
) {
    head {
        title(title)

        style {
            unsafe {
                //language=CSS
                +"""
                body {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    height: 100vh;
                    margin: 0;
                    color: ${textColor.hex()};
                    overflow: hidden;
                    position: relative;
                    background: linear-gradient(45deg, ${firstAccentColor.hex()}, ${secondAccentColor.hex()});
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