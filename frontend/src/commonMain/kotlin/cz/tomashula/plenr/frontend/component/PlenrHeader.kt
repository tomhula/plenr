package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.Logo
import dev.kilua.compose.foundation.layout.Arrangement
import dev.kilua.compose.foundation.layout.Row
import dev.kilua.compose.ui.Alignment
import dev.kilua.core.IComponent
import dev.kilua.html.AlignItems
import dev.kilua.html.Background
import dev.kilua.html.Color
import dev.kilua.html.Cursor
import dev.kilua.html.FontWeight
import dev.kilua.html.JustifyContent
import dev.kilua.html.Position
import dev.kilua.html.div
import dev.kilua.html.header
import dev.kilua.html.img
import dev.kilua.html.li
import dev.kilua.html.link
import dev.kilua.html.perc
import dev.kilua.html.px
import dev.kilua.html.rem
import dev.kilua.html.spant
import dev.kilua.html.style.pClass
import dev.kilua.html.style.pElement
import dev.kilua.html.style.style
import dev.kilua.html.ul
import dev.kilua.panel.hPanel

@Composable
fun IComponent.plenrHeader(
    title: String,
    user: UserDto?,
    onLogoClick: () -> Unit = {},
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
            img(Logo.url) {
                zIndex(1)
                cursor(Cursor.Pointer)
                onClick { onLogoClick() }
            }

            hPanel(
                justifyContent = JustifyContent.Center,
                alignItems = AlignItems.Center,
            ) {
                position(Position.Absolute)
                width(100.perc)
                height(100.perc)

                spant(title) {
                    fontSize(1.3.rem)
                    fontWeight(FontWeight.Bold)
                }
            }

            user?.let { user ->
                div("dropdown") {
                    hPanel(
                        alignItems = AlignItems.Center,
                    ) {
                        attribute("data-bs-toggle", "dropdown")
                        cursor(Cursor.Pointer)

                        spant(user.fullName) {
                            marginRight(16.px)
                        }
                        materialIconOutlined("account_circle")
                    }
                    ul("dropdown-menu") {
                        if (!user.isAdmin)
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
}
