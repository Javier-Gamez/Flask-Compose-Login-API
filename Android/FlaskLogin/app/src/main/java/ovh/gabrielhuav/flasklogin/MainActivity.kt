package ovh.gabrielhuav.flasklogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ovh.gabrielhuav.flasklogin.ui.AppViewModel
import ovh.gabrielhuav.flasklogin.ui.screens.LoginScreen
import ovh.gabrielhuav.flasklogin.ui.screens.NotesScreen
import ovh.gabrielhuav.flasklogin.ui.screens.RegisterScreen
import ovh.gabrielhuav.flasklogin.ui.theme.FlaskLoginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlaskLoginTheme {
                AppRoot()
            }
        }
    }
}

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_NOTES = "notes"

/**
 * Navega reemplazando TODO el historial previo por [route]. Se usa en las
 * transiciones de autenticación (login/logout) para que el botón "atrás"
 * del sistema no pueda revelar pantallas de una sesión distinta.
 */
private fun NavHostController.replaceStackWith(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(viewModel: AppViewModel = viewModel()) {
    val navController: NavHostController = rememberNavController()
    val token by viewModel.token.collectAsState()
    val username by viewModel.username.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (viewModel.awaitRestoredToken() != null) {
            navController.replaceStackWith(ROUTE_NOTES)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FlaskLogin Notas") },
                actions = {
                    TextButton(onClick = { menuExpanded = true }) {
                        Text("Menú")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        if (token == null) {
                            DropdownMenuItem(
                                text = { Text("Inicio de sesión") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(ROUTE_LOGIN) { launchSingleTop = true }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Registro de usuario") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(ROUTE_REGISTER) { launchSingleTop = true }
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Notas") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(ROUTE_NOTES) { launchSingleTop = true }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión (${username ?: ""})") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.logout()
                                    navController.replaceStackWith(ROUTE_LOGIN)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoggedIn = { navController.replaceStackWith(ROUTE_NOTES) },
                    onNavigateRegister = { navController.navigate(ROUTE_REGISTER) { launchSingleTop = true } }
                )
            }
            composable(ROUTE_REGISTER) {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegistered = { navController.replaceStackWith(ROUTE_LOGIN) },
                    onNavigateLogin = { navController.navigate(ROUTE_LOGIN) { launchSingleTop = true } }
                )
            }
            composable(ROUTE_NOTES) {
                NotesScreen(viewModel = viewModel)
            }
        }
    }
}
