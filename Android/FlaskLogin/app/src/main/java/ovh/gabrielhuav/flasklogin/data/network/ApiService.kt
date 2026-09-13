package ovh.gabrielhuav.flasklogin.data.network

import ovh.gabrielhuav.flasklogin.data.model.AuthRequest
import ovh.gabrielhuav.flasklogin.data.model.LoginResponse
import ovh.gabrielhuav.flasklogin.data.model.MessageResponse
import ovh.gabrielhuav.flasklogin.data.model.Note
import ovh.gabrielhuav.flasklogin.data.model.NoteRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("register")
    suspend fun register(@Body body: AuthRequest): Response<MessageResponse>

    @POST("login")
    suspend fun login(@Body body: AuthRequest): Response<LoginResponse>

    @GET("notes")
    suspend fun getNotes(@Header("Authorization") authHeader: String): Response<List<Note>>

    @POST("notes")
    suspend fun createNote(
        @Header("Authorization") authHeader: String,
        @Body body: NoteRequest
    ): Response<Note>

    @PUT("notes/{id}")
    suspend fun updateNote(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int,
        @Body body: NoteRequest
    ): Response<Note>

    @DELETE("notes/{id}")
    suspend fun deleteNote(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): Response<MessageResponse>
}
