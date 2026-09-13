package ovh.gabrielhuav.flasklogin.data.model

data class AuthRequest(
    val username: String,
    val password: String
)

data class MessageResponse(
    val message: String?
)

data class LoginResponse(
    val status: String?,
    val message: String?,
    val token: String?,
    val user_id: Int?,
    val username: String?
)

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val created_at: String,
    val updated_at: String
)

data class NoteRequest(
    val title: String,
    val content: String
)
