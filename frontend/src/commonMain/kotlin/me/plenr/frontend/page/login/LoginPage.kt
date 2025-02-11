package me.plenr.frontend.page.login

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.form
import dev.kilua.html.*
import kotlinx.coroutines.launch
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.applyColumn
import me.plenr.frontend.component.formField
import me.plenr.frontend.component.onSubmit

@Composable
fun IComponent.loginPage(mainViewModel: MainViewModel)
{
    val coroutineScope = rememberCoroutineScope()
    val router = Router.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authenticated by remember { mutableStateOf<Boolean?>(null) }

    form(className = "form") {
        applyColumn(alignItems = AlignItems.Center)
        rowGap(10.px)

        onSubmit {
            coroutineScope.launch {
                authenticated = mainViewModel.login(email, password)
                if (authenticated == true)
                    router.navigate("/")
            }
        }

        h1t("Login", "form-header")

        formField(
            inputId = "email-field",
            label = "Email",
            value = email,
            type = InputType.Email,
            onChange = { email = it }
        )
        formField(
            inputId = "password-field",
            label = "Password",
            value = password,
            type = InputType.Password,
            onChange = { password = it }
        )
        button("Login", className = "primary-button", type = ButtonType.Submit)

        when (authenticated)
        {
            false -> p { +"Incorrect credentials" }
            else -> Unit
        }
    }
}