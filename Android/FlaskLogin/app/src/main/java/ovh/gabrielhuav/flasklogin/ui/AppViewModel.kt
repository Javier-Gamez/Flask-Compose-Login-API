package ovh.gabrielhuav.flasklogin.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ovh.gabrielhuav.flasklogin.data.SessionManager
import ovh.gabrielhuav.flasklogin.data.model.AuthRequest
import ovh.gabrielhuav.flasklogin.data.model.Note
import ovh.gabrielhuav.flasklogin.data.model.NoteRequest
import ovh.gabrielhuav.flasklogin.data.network.RetrofitInstance
import retrofit2.Response

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val api = RetrofitInstance.api

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    /** Completa cuando la sesion persistida (si existe) ya fue cargada desde DataStore. */
    private val sessionRestored = viewModelScope.launch {
        _token.value = sessionManager.tokenFlow.first()
        _username.value = sessionManager.usernameFlow.first()
    }

    /** Espera a que termine de leerse la sesion guardada y devuelve el token resultante. */
    suspend fun awaitRestoredToken(): String? {
        sessionRestored.join()
        return _token.value
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun register(username: String, password: String, onResult: (Boolean) -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Usuario y contraseña son obligatorios"
            onResult(false)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.register(AuthRequest(username, password))
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    _errorMessage.value = extractError(response, "No se pudo registrar el usuario")
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Usuario y contraseña son obligatorios"
            onResult(false)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.login(AuthRequest(username, password))
                val body = response.body()
                if (response.isSuccessful && body?.token != null) {
                    sessionManager.saveSession(body.token, body.username ?: username)
                    _token.value = body.token
                    _username.value = body.username ?: username
                    onResult(true)
                } else {
                    _errorMessage.value = extractError(response, "Credenciales inválidas")
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _token.value = null
            _username.value = null
            _notes.value = emptyList()
        }
    }

    fun loadNotes() {
        val currentToken = _token.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.getNotes(bearer(currentToken))
                if (response.isSuccessful) {
                    _notes.value = response.body().orEmpty()
                } else if (response.code() == 401) {
                    handleUnauthorized()
                } else {
                    _errorMessage.value = extractError(response, "No se pudieron cargar las notas")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNote(title: String, content: String, onResult: (Boolean) -> Unit) {
        val currentToken = _token.value ?: return
        if (title.isBlank()) {
            _errorMessage.value = "El título es obligatorio"
            onResult(false)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.createNote(bearer(currentToken), NoteRequest(title, content))
                if (response.isSuccessful) {
                    loadNotes()
                    onResult(true)
                } else if (response.code() == 401) {
                    handleUnauthorized()
                    onResult(false)
                } else {
                    _errorMessage.value = extractError(response, "No se pudo crear la nota")
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(id: Int, title: String, content: String, onResult: (Boolean) -> Unit) {
        val currentToken = _token.value ?: return
        if (title.isBlank()) {
            _errorMessage.value = "El título es obligatorio"
            onResult(false)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.updateNote(bearer(currentToken), id, NoteRequest(title, content))
                if (response.isSuccessful) {
                    loadNotes()
                    onResult(true)
                } else if (response.code() == 401) {
                    handleUnauthorized()
                    onResult(false)
                } else {
                    _errorMessage.value = extractError(response, "No se pudo actualizar la nota")
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(id: Int) {
        val currentToken = _token.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.deleteNote(bearer(currentToken), id)
                if (response.isSuccessful) {
                    loadNotes()
                } else if (response.code() == 401) {
                    handleUnauthorized()
                } else {
                    _errorMessage.value = extractError(response, "No se pudo eliminar la nota")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun handleUnauthorized() {
        sessionManager.clearSession()
        _token.value = null
        _username.value = null
        _notes.value = emptyList()
        _errorMessage.value = "Sesión expirada, vuelve a iniciar sesión"
    }

    private fun bearer(token: String) = "Bearer $token"

    private fun <T> extractError(response: Response<T>, fallback: String): String {
        val raw = response.errorBody()?.string()
        if (raw.isNullOrBlank()) return fallback
        return Regex("\"message\"\\s*:\\s*\"([^\"]*)\"").find(raw)?.groupValues?.get(1) ?: fallback
    }
}
