package com.futureme.shared.design

import kotlinx.serialization.Serializable

@Serializable
data class SpacingTokens(
    val xs: Int = 4,
    val sm: Int = 8,
    val md: Int = 16,
    val lg: Int = 24,
    val xl: Int = 32,
)

@Serializable
data class RadiusTokens(
    val small: Int = 10,
    val medium: Int = 16,
    val large: Int = 24,
    val pill: Int = 999,
)

@Serializable
data class TypographyTokens(
    val display: String = "display",
    val headline: String = "headline",
    val title: String = "title",
    val body: String = "body",
    val label: String = "label",
)

@Serializable
data class ColorRoleTokens(
    val brand: String = "#153B30",
    val accent: String = "#61C99E",
    val positive: String = "#23805E",
    val warning: String = "#B05E48",
    val canvasLight: String = "#F4F7F3",
    val canvasDark: String = "#0D1612",
    val surfaceLight: String = "#FFFFFF",
    val surfaceDark: String = "#14211C",
)

@Serializable
data class CardStyleTokens(
    val radiusRole: String = "large",
    val borderRole: String = "subtle",
    val elevationRole: String = "low",
)

@Serializable
data class DesignTokens(
    val spacing: SpacingTokens = SpacingTokens(),
    val radius: RadiusTokens = RadiusTokens(),
    val typography: TypographyTokens = TypographyTokens(),
    val colors: ColorRoleTokens = ColorRoleTokens(),
    val card: CardStyleTokens = CardStyleTokens(),
)

object FutureMeDesignTokens {
    val current = DesignTokens()
}
