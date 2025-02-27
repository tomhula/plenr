package cz.tomashula.plenr.frontend

import dev.kilua.html.Color

object Colors
{
    const val DRESSAGE_TRAINING_BACKGROUND = "#c1d3ff"
    const val PARKOUR_TRAINING_BACKGROUND = "#fbb78e"
    val PEOPLE_COLORS = listOf(
        Color.hex(0x1E90FF),
        Color.hex(0xFF4500),
        Color.hex(0x32CD32),
        Color.hex(0xFFD700),
        Color.hex(0x8A2BE2),
        Color.hex(0xFF1493),
        Color.hex(0x20B2AA),
        Color.hex(0xFF6347),
        Color.hex(0x4682B4),
        Color.hex(0xDA70D6),
        Color.hex(0x00FA9A),
        Color.hex(0xFFA500),
        Color.hex(0x6A5ACD),
        Color.hex(0xDC143C),
        Color.hex(0x3CB371),
        Color.hex(0xB22222),
        Color.hex(0x2E8B57),
        Color.hex(0x8B008B),
        Color.hex(0xD2691E),
        Color.hex(0x5F9EA0)
    )

    fun getColorForPerson(input: String): Color
    {
        val hash = input.hashCode()
        val index = (hash % PEOPLE_COLORS.size).let { if (it < 0) it + PEOPLE_COLORS.size else it } // Ensure positive index
        return PEOPLE_COLORS[index]
    }
}
