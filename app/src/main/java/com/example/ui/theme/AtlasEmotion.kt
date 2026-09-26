package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Sistema de Emociones y Estados Visuales de ATLAS
 *
 * BLUE (HAPPY): Alegría, curiosidad, energía, optimismo
 * GREEN (PHILOSOPHICAL): Reflexión, filosofía, conocimiento, calma intelectual
 * WHITE (SERENE): Serenidad, intuición, claridad, minimalismo
 * BLACK (CAPABLE): Capacidad, integridad, código, concentración, sigilo
 * RED (SENTINEL): Seguridad, diagnóstico, alerta máxima, supervisión técnica
 */
enum class AtlasEmotion(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val bgDark: Color,
    val surfaceDark: Color,
    val cardDark: Color,
    val cardElevated: Color,
    val borderDark: Color,
    val borderGlow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
) {
    HAPPY(
        title = "Alegre",
        subtitle = "Curiosidad y energía creativa",
        primaryColor = Color(0xFF00E5FF),     // Azul Cyan eléctrico vibrante
        secondaryColor = Color(0xFF2979FF),   // Azul Real
        glowColor = Color(0x4000E5FF),
        bgDark = Color(0xFF040B14),
        surfaceDark = Color(0xFF081426),
        cardDark = Color(0xFF0E1E38),
        cardElevated = Color(0xFF142B4E),
        borderDark = Color(0xFF1A3966),
        borderGlow = Color(0x4D00E5FF),
        textPrimary = Color(0xFFF0F7FF),
        textSecondary = Color(0xFFA1C6EA),
        textMuted = Color(0xFF5D7B9D)
    ),
    PHILOSOPHICAL(
        title = "Pensativo",
        subtitle = "Filosofía y reflexión profunda",
        primaryColor = Color(0xFF00E676),     // Verde Neón vibrante
        secondaryColor = Color(0xFF10B981),   // Verde Esmeralda
        glowColor = Color(0x4000E676),
        bgDark = Color(0xFF060B08),
        surfaceDark = Color(0xFF0C1610),
        cardDark = Color(0xFF102016),
        cardElevated = Color(0xFF162B1F),
        borderDark = Color(0xFF1B3D27),
        borderGlow = Color(0x4D00E676),
        textPrimary = Color(0xFFF1FDF5),
        textSecondary = Color(0xFFA7D7B5),
        textMuted = Color(0xFF527963)
    ),
    SERENE(
        title = "Sereno",
        subtitle = "Claridad y equilibrio intuitivo",
        primaryColor = Color(0xFFE2E8F0),     // Plata brillante
        secondaryColor = Color(0xFF94A3B8),   // Gris platino
        glowColor = Color(0x35E2E8F0),
        bgDark = Color(0xFF090D12),
        surfaceDark = Color(0xFF131922),
        cardDark = Color(0xFF1C2430),
        cardElevated = Color(0xFF263242),
        borderDark = Color(0xFF334155),
        borderGlow = Color(0x4DE2E8F0),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFFCBD5E1),
        textMuted = Color(0xFF64748B)
    ),
    CAPABLE(
        title = "Capaz",
        subtitle = "Integridad y máxima concentración",
        primaryColor = Color(0xFF93C5FD),     // Acento metálico frío
        secondaryColor = Color(0xFF60A5FA),
        glowColor = Color(0x3060A5FA),
        bgDark = Color(0xFF020406),           // Negro absoluto carbón
        surfaceDark = Color(0xFF0A0D12),
        cardDark = Color(0xFF111722),
        cardElevated = Color(0xFF182233),
        borderDark = Color(0xFF202C3F),
        borderGlow = Color(0x4060A5FA),
        textPrimary = Color(0xFFF3F4F6),
        textSecondary = Color(0xFF9CA3AF),
        textMuted = Color(0xFF4B5563)
    ),
    SENTINEL(
        title = "Centinela",
        subtitle = "Diagnóstico y alerta máxima de seguridad",
        primaryColor = Color(0xFFFF1744),     // Rojo Carmesí Alerta Neón
        secondaryColor = Color(0xFFD50000),   // Rojo Intenso
        glowColor = Color(0x50FF1744),
        bgDark = Color(0xFF0D0204),           // Negro rojizo
        surfaceDark = Color(0xFF1A0508),
        cardDark = Color(0xFF26080D),
        cardElevated = Color(0xFF360C13),
        borderDark = Color(0xFF52121C),
        borderGlow = Color(0x66FF1744),
        textPrimary = Color(0xFFFFF1F2),
        textSecondary = Color(0xFFFDA4AF),
        textMuted = Color(0xFF9F1239)
    )
}
