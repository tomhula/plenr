package me.plenr.frontend.page.admin

import androidx.compose.runtime.Composable
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.html.button
import dev.kilua.html.h2t
import me.plenr.frontend.MainViewModel

@Composable
fun IComponent.adminHomePage(viewModel: MainViewModel)
{
    val router = Router.current

    button("Manage users") {
        onClick {
            router.navigate("/manage-users")
        }
    }
    button("Arrange workouts") {
        onClick {
            router.navigate("/arrange-workouts")
        }
    }

    h2t("This week:")
}