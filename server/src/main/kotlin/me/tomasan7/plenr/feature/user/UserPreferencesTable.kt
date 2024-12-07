package me.tomasan7.plenr.feature.user

import org.jetbrains.exposed.dao.id.IntIdTable

object UserPreferencesTable: IntIdTable("user_preferences")
{
    val userId = reference("user_id", UserTable)
    val trainingsPerWeek = integer("trainings_per_week")
    val trainingArrangedNotiEmail = bool("training_arranged_noti_email")
    val trainingArrangedNotiSms = bool("training_arranged_noti_sms")
    val trainingMovedNotiEmail = bool("training_moved_noti_email")
    val trainingMovedNotiSms = bool("training_moved_noti_sms")
    val trainingCancelledNotiEmail = bool("training_cancelled_noti_email")
    val trainingCancelledNotiSms = bool("training_cancelled_noti_sms")
}