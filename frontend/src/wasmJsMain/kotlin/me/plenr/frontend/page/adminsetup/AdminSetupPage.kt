package me.plenr.frontend.page.adminsetup

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.text.text
import dev.kilua.html.h1t
import dev.kilua.html.h3t
import me.plenr.frontend.PlenrClient

@Composable
fun IComponent.AdminSetupPage(plenrClient: PlenrClient)
{
    var textValue by remember { mutableStateOf("") }

    h1t("Admin Setup")
    text(
        type = InputType.Email,
        value = textValue,
    ) {
        onInput {
            textValue = this.value ?: ""
        }
    }

    h3t(textValue)
}