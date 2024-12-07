package me.tomasan7.plenr.serialization

import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.tomasan7.plenr.util.Week

object WeekSerializer : KSerializer<Week>
{
    override val descriptor = LocalDate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Week)
    {
        return LocalDate.serializer().serialize(encoder, value.mondayDate)
    }

    override fun deserialize(decoder: Decoder): Week
    {
        return Week(LocalDate.serializer().deserialize(decoder))
    }
}