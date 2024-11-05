package me.plenr.frontend.page.passwordsetup

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.form
import dev.kilua.form.text.text
import dev.kilua.html.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import me.plenr.frontend.PlenrClient
import me.plenr.frontend.component.applyColumn
import me.plenr.frontend.component.onSubmit
import web.window

@Composable
fun IComponent.passwordSetupPage(plenrClient: PlenrClient, token: String)
{
    val coroutineScope = rememberCoroutineScope()
    val router = Router.current

    val tokenUrlDecoded = token.decodeURLPart()

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    form {
        applyColumn(alignItems = AlignItems.Center)
        onSubmit {
            if (password != confirmPassword)
            {
                window.alert("Passwords do not match")
                return@onSubmit
            }

            coroutineScope.launch {
                plenrClient.setPassword(tokenUrlDecoded, password)
                router.navigate("/login")
            }
        }

        h1t("Password Setup")

        div {
            applyColumn()
            label {
                htmlFor("password")
                +"Password"
            }
            text(password, id = "password") {
                onInput {
                    password = this.value ?: ""
                }
                type(InputType.Password)
            }
        }

        div {
            applyColumn()
            label {
                htmlFor("confirm-password")
                +"Confirm Password"
            }
            text(confirmPassword, id = "confirm-password") {
                onInput {
                    confirmPassword = this.value ?: ""
                }
                type(InputType.Password)
            }
        }

        button {
            type(ButtonType.Submit)
            +"Set Password"
        }
    }
}