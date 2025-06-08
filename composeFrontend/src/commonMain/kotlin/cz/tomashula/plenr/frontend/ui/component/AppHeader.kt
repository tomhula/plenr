package cz.tomashula.plenr.frontend.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tomashula.plenr.feature.user.UserDto
import me.tomasan7.composefrontend.generated.resources.Res
import me.tomasan7.composefrontend.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppHeader(
    title: String,
    user: UserDto? = null,
    onLogoClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier,
)
{
    Surface(
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .clickable(onClick = onLogoClick)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
            )
            user?.let {
                LoggedInUser(
                    user = it,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun LoggedInUser(
    user: UserDto,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier,
)
{
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { menuExpanded = !menuExpanded }
                .padding(8.dp)
        ) {
            Text(user.fullName)
            Icon(Icons.Outlined.AccountCircle, contentDescription = "User Profile")
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Log out") },
                onClick = {
                    menuExpanded = false
                    onLogoutClick()
                }
            )
        }
    }
}
