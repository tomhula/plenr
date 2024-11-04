package me.plenr.frontend.page.login

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.form
import dev.kilua.form.text.text
import dev.kilua.html.*
import kotlinx.coroutines.launch
import me.plenr.frontend.PlenrClient
import me.plenr.frontend.component.applyColumn
import me.plenr.frontend.component.onSubmit

@Composable
fun IComponent.loginPage(plenrClient: PlenrClient)
{
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authenticated by remember { mutableStateOf<Boolean?>(null) }

    form {
        applyColumn(alignItems = AlignItems.Center)

        onSubmit {
            coroutineScope.launch {
                authenticated = plenrClient.login(email, password)
            }
        }

        h1 {
            +"Login"
        }
        div {
            applyColumn()
            label {
                htmlFor("email")
                +"Email"
            }
            text(id = "email") {
                type(InputType.Email)
                onChange {
                    email = this.value ?: ""
                }
            }
        }
        div {
            applyColumn()
            label {
                htmlFor("password")
                +"Password"
            }
            text(id = "password") {
                type(InputType.Password)
                onChange {
                    password = this.value ?: ""
                }
            }
        }
        button {
            type(ButtonType.Submit)
            +"Login"
        }

        when (authenticated)
        {
            true -> p { +"Authenticated" }
            false -> p { +"Incorrect credentials" }
            null -> {}
        }
    }
}