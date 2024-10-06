package me.tomasan7

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import me.tomasan7.plenr.adduser.AddUserScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(AddUserScreen())
    }
}