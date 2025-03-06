package cz.tomashula.plenr.frontend

import dev.kilua.html.Color

object Colors
{
    val DRESSAGE_TRAINING_BACKGROUND = Color.hex(0xC1D3FF)
    val PARKOUR_TRAINING_BACKGROUND = Color.hex(0xFBB78E)
    val COLOR_PALETTE = listOf(
        Color.hex(0x8DD3C7),
        Color.hex(0xFFFFB3),
        Color.hex(0xBEBADA),
        Color.hex(0xFB8072),
        Color.hex(0x80B1D3),
        Color.hex(0xFDB462),
        Color.hex(0xB3DE69),
        Color.hex(0xFCCDE5),
        Color.hex(0xD9D9D9),
        Color.hex(0xBC80BD),
        Color.hex(0xCCECB5)
    )

    fun getColor(any: Any): Color
    {
        val hash = any.hashCode()
        val index = (hash % COLOR_PALETTE.size).let { if (it < 0) it + COLOR_PALETTE.size else it } // Ensure positive index
        return COLOR_PALETTE[index]
    }
}
