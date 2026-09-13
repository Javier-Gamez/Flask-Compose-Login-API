package ovh.gabrielhuav.flasklogin.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USERNAME = stringPreferencesKey("username")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }
    val usernameFlow: Flow<String?> = context.dataStore.data.map { it[Keys.USERNAME] }

    suspend fun saveSession(token: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USERNAME] = username
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
