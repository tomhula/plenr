package me.tomasan7.plenr.adduser

import androidx.compose.ui.text.input.TextFieldValue

data class AddUserScreenState(
    val name: String = "",
    val surname: String = "",
    val phone: TextFieldValue = TextFieldValue(""),
    val email: String = ""
)
{
    constructor(
        name: String = "",
        surname: String = "",
        phone: String = "",
        email: String = ""
    ) : this(
        name = name,
        surname = surname,
        phone = TextFieldValue(phone),
        email = email
    )
}