package me.plenr.frontend.page.admin

import androidx.compose.runtime.Composable
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.html.button
import dev.kilua.html.div
import dev.kilua.html.h2t
import dev.kilua.html.px
import me.plenr.frontend.MainViewModel
import me.plenr.frontend.component.applyColumn

@Composable
fun IComponent.adminHomePage(viewModel: MainViewModel)
{
    val router = Router.current

    div {
        applyColumn()
        rowGap(10.px)
        button("Manage users", className = "primary-button") {
            onClick {
                router.navigate("/manage-users")
            }
        }
        button("Arrange trainings", className = "primary-button") {
            onClick {
                router.navigate("/arrange-trainings")
            }
        }
        h2t("This week:")
    }
}