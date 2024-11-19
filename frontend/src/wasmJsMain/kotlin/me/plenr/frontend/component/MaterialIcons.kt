package me.plenr.frontend.component

import androidx.compose.runtime.Composable
import dev.kilua.core.IComponent
import dev.kilua.html.spant

@Composable
fun IComponent.materialIconOutlined(name: String)
{
    spant(className = "material-symbols-outlined", text = name)
}