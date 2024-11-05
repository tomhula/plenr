package me.plenr.frontend.page

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.h1t
import me.plenr.frontend.PlenrClient

@Composable
fun IComponent.homePage(plenrClient: PlenrClient)
{
    h1t("Welcome ${plenrClient.user?.name}")
}