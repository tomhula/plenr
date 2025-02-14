package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import cz.tomashula.plenr.frontend.Logo
import dev.kilua.core.IComponent
import dev.kilua.html.AlignItems
import dev.kilua.html.Background
import dev.kilua.html.Color
import dev.kilua.html.JustifyContent
import dev.kilua.html.Position
import dev.kilua.html.div
import dev.kilua.html.header
import dev.kilua.html.img
import dev.kilua.html.li
import dev.kilua.html.link
import dev.kilua.html.perc
import dev.kilua.html.spant
import dev.kilua.html.ul
import dev.kilua.panel.hPanel

@Composable
fun IComponent.plenrHeader(
    title: String,
    isAdmin: Boolean,
    onUnavailableDaysClick: () -> Unit = {},
    onPreferencesClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
)
{
    @Composable
    fun IComponent.dropDownItem(
        text: String,
        onClick: () -> Unit
    )
    {
        li {
            link(className = "dropdown-item") {
                role("button")
                onClick {
                    onClick()
                }
                +text
            }
        }
    }

    header {
        width(100.perc)
        background(Background(Color("#f8f9fa")))
        style("padding", "10px 20px")

        hPanel(
            justifyContent = JustifyContent.SpaceBetween,
            alignItems = AlignItems.Center,
        ) {
            position(Position.Relative)
            img(Logo.url)

            hPanel(
                justifyContent = JustifyContent.Center,
                alignItems = AlignItems.Center,
            ) {
                position(Position.Absolute)
                width(100.perc)
                height(100.perc)

                spant(title)
            }

            div("dropdown") {
                materialIconOutlined("account_circle") {
                    attribute("data-bs-toggle", "dropdown")
                }
                ul("dropdown-menu") {
                    if (!isAdmin)
                        dropDownItem(
                            text = "Unavailable Days",
                            onClick = onUnavailableDaysClick
                        )
                    dropDownItem(
                        text = "Preferences",
                        onClick = onPreferencesClick
                    )
                    dropDownItem(
                        text = "Log out",
                        onClick = onLogoutClick
                    )
                }
            }
        }
    }
}
