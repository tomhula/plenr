package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.Composable
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.html.button
import dev.kilua.html.div
import dev.kilua.html.h2t
import dev.kilua.html.px
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.applyColumn
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Column
import dev.kilua.compose.foundation.layout.Row
import dev.kilua.html.bsButton

@Composable
fun IComponent.adminHomePage(viewModel: MainViewModel)
{
    val router = Router.current

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.px),
        ) {
            bsButton("Manage users") {
                onClick {
                    router.navigate(Route.MANAGE_USERS)
                }
            }
            bsButton("Arrange trainings") {
                onClick {
                    router.navigate(Route.ARRANGE_TRAININGS)
                }
            }
        }
        h2t("This week:")
    }
}
