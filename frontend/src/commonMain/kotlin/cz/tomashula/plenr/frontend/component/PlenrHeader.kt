package cz.tomashula.plenr.frontend.component

import androidx.compose.runtime.Composable
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.Logo
import dev.kilua.core.IComponent
import dev.kilua.html.*
import dev.kilua.panel.hPanel

@Composable
fun IComponent.plenrHeader(
    title: String,
    user: UserDto?,
    onLogoClick: () -> Unit = {},
    onAvailabilityClick: () -> Unit = {},
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
                                text = "Availability",
                                onClick = onAvailabilityClick
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
