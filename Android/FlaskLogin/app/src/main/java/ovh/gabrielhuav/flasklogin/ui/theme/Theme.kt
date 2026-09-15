package ovh.gabrielhuav.flasklogin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// La app usa siempre un esquema oscuro fijo (no depende del tema del
// sistema ni de colores dinamicos) para que se vea igual en cualquier
// dispositivo, incluyendo el celular fisico.
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    error = DarkError,
    onError = DarkOnError,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun FlaskLoginTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}