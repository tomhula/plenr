package cz.tomashula.plenr.frontend.ui

import androidx.compose.ui.graphics.Color

object Colors {
    val DRESSAGE_TRAINING_BACKGROUND = Color(0xFFC1D3FF)
    val PARKOUR_TRAINING_BACKGROUND = Color(0xFFFBB78E)
    val COLOR_PALETTE = listOf(
        Color(0xFF8DD3C7),
        Color(0xFFFFFFB3),
        Color(0xFFBEBADA),
        Color(0xFFFB8072),
        Color(0xFF80B1D3),
        Color(0xFFFDB462),
        Color(0xFFB3DE69),
        Color(0xFFFCCDE5),
        Color(0xFFD9D9D9),
        Color(0xFFBC80BD),
        Color(0xFFCCEBC5),
        Color(0xFFFFED6F),
    )

    fun getColor(any: Any): Color {
        val hash = any.hashCode()
        val index = (hash % COLOR_PALETTE.size).let { if (it < 0) it + COLOR_PALETTE.size else it } // Ensure positive index
        return COLOR_PALETTE[index]
    }
}
