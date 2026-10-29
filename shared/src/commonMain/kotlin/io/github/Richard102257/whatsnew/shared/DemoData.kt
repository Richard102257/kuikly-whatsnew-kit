package io.github.Richard102257.whatsnew.shared

import io.github.Richard102257.whatsnew.*

/**
 * Demo 共享数据 —— 定义多版本 WhatsNew 内容
 */
val demoWhatsNewCollection: List<WhatsNew> = whatsNewCollection {
    whatsNew("1.0.0") {
        title("Welcome")
        feature(
            icon = WhatsNewIcon.SystemIcon("star.fill"),
            title = "Cross-Platform Development",
            subtitle = "Write once, run everywhere with native performance."
        )
        feature(
            icon = WhatsNewIcon.SystemIcon("bolt.fill"),
            title = "Lightning Fast",
            subtitle = "Native rendering ensures the best experience."
        )
        primaryAction {
            title = "Get Started"
            backgroundColor = 0xFF6200EE
            foregroundColor = 0xFFFFFFFF
        }
    }

    whatsNew("2.0.0") {
        title("What's New in v2.0")
        feature(
            icon = WhatsNewIcon.SystemIcon("wand.and.stars"),
            title = "AI-Powered Development",
            subtitle = "Kuikly integrates with AI tools for smarter code generation."
        )
        feature(
            icon = WhatsNewIcon.SystemIcon("globe"),
            title = "More Platforms",
            subtitle = "Now supporting Web (H5) and Mini Programs."
        )
        feature(
            icon = WhatsNewIcon.SystemIcon("speedometer"),
            title = "Performance Boost",
            subtitle = "Optimized rendering pipeline for even faster performance."
        )
        feature(
            icon = WhatsNewIcon.SystemIcon("paintbrush.fill"),
            title = "Compose DSL",
            subtitle = "Write cross-platform UI using standard Jetpack Compose DSL."
        )
        primaryAction {
            title = "Continue"
            backgroundColor = 0xFF6200EE
            foregroundColor = 0xFFFFFFFF
        }
        secondaryAction {
            title = "Learn More"
            foregroundColor = 0xFF6200EE
            openURL("https://kuikly.tds.qq.com")
        }
    }
}
