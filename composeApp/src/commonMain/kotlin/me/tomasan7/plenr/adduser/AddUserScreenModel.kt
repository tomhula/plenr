package me.tomasan7.plenr.adduser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.ScreenModel

/* TODO: Make the phone prefix configurable */
private const val DEFAULT_PHONE_PREFIX = "+420 "

class AddUserScreenModel : ScreenModel
{
    var uiState by mutableStateOf(
        AddUserScreenState(
            phone = TextFieldValue(
                DEFAULT_PHONE_PREFIX, selection = TextRange(DEFAULT_PHONE_PREFIX.length)
            )
        )
    )
        private set

    fun setName(name: String)
    {
        uiState = uiState.copy(name = name.trim())
    }

    fun setSurname(surname: String)
    {
        uiState = uiState.copy(surname = surname.trim())
    }

    fun setPhone(phone: TextFieldValue)
    {
        val newPhone = formatPhone(phone.text)
        uiState = uiState.copy(phone = TextFieldValue(text = newPhone, selection = TextRange(newPhone.length)))
    }

    fun setPhone(phone: String)
    {
        setPhone(TextFieldValue(phone))
    }

    fun setEmail(email: String)
    {
        uiState = uiState.copy(email = email)
    }

    fun onAddUserClick()
    {
        println("Add user clicked")
    }

    private fun formatPhone(phone: String): String
    {
        val sb = StringBuilder()

        phone.forEach { char -> if (sb.length <= 12 && (char == '+' || char.isDigit())) sb.append(char) }

        for (spaceI in listOf(4, 8, 12))
            if (sb.length >= spaceI)
                sb.insert(spaceI, ' ')

        val sbStr = sb.toString()

        return if (sbStr.endsWith(' ') && !phone.endsWith(' '))
            sbStr.dropLast(1)
        else
            sbStr
    }
}