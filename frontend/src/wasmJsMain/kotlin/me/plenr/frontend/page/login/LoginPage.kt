package me.plenr.frontend.page.login

import androidx.compose.runtime.*
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.text.text
import dev.kilua.html.*
import dev.kilua.html.helpers.onClickLaunch
import kotlinx.browser.window
import me.plenr.frontend.PlenrClient
import me.plenr.frontend.component.column

@Composable
fun IComponent.loginPage(plenrClient: PlenrClient)
{
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    column(
        alignItems = AlignItems.Center
    ) {
        h1 {
            +"Login"
        }
        column {
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
        column {
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
            +"Login"
            onClickLaunch(coroutineScope) {
                window.alert(plenrClient.login(email, password).toString())
            }
        }
    }
}