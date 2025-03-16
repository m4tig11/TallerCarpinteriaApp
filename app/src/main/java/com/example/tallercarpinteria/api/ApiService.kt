package com.example.tallercarpinteria.api

import android.util.Log
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.io.File

private const val TAG = "ApiService"
private const val BASE_URL = "http://10.0.2.2:8000/api/"  // localhost en Android

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class PedidoUpdate(
    val cliente_nombre: String,
    val estado: String,
    val fecha_medicion: String,
    val fecha_presupuesto: String? = null,
    val fecha_materiales: String? = null,
    val fecha_entrega: String? = null,
    val notas: String? = null,
    val presupuesto: Double? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val detalles_medicion: String? = null,
    val lista_materiales: String? = null,
    val observaciones: String? = null,
    val plano: String? = null
)

interface ApiService {
    @POST("token/")
    suspend fun login(@Body credentials: LoginRequest): Response<TokenResponse>

    @GET("pedidos/")
    suspend fun getPedidos(@Header("Authorization") token: String): Response<List<Pedido>>

    @GET("pedidos/{id}/")
    suspend fun getPedido(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int
    ): Response<Pedido>

    @Multipart
    @PATCH("pedidos/{id}/")
    suspend fun updatePedidoWithImage(
        @Header("Authorization") token: String,
        @Path("id") pedidoId: Int,
        @Part("data") pedidoData: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<Pedido>

    @GET("pedidos")
    suspend fun getPedidos(): Response<List<Pedido>>

    @PUT("pedidos/{id}")
    suspend fun updatePedido(
        @Path("id") id: Int,
        @Body pedido: Pedido
    ): Response<Pedido>

    companion object {
        private var instance: ApiService? = null
        private var accessToken: String? = null
        private var refreshToken: String? = null

        fun getInstance(): ApiService {
            if (instance == null) {
                val logging = HttpLoggingInterceptor { message ->
                    Log.d(TAG, message)
                }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                instance = retrofit.create(ApiService::class.java)
                Log.d(TAG, "📱 ApiService inicializado")
            }
            return instance!!
        }

        fun setToken(token: String) {
            accessToken = token
            Log.d(TAG, "🔑 Token establecido: ${token.take(20)}...")
        }

        fun getAuthHeader(): String {
            return "Bearer $accessToken"
        }

        fun isAuthenticated(): Boolean {
            return !accessToken.isNullOrEmpty()
        }

        fun createMultipartImage(imageFile: File): MultipartBody.Part {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData("plano", imageFile.name, requestFile)
        }

        fun createPedidoRequestBody(pedidoUpdate: PedidoUpdate): RequestBody {
            val json = Gson().toJson(pedidoUpdate)
            return json.toRequestBody("application/json".toMediaTypeOrNull())
        }
    }
}

data class LoginRequest(
    val username: String,
    val password: String
)

// Clase para manejar la respuesta de la API
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

// Extensión para manejar las respuestas de manera más limpia
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            Log.d(TAG, "✅ Llamada API exitosa")
            ApiResult.Success(response.body()!!)
        } else {
            Log.e(TAG, "❌ Error en la llamada API: ${response.code()} - ${response.message()}")
            ApiResult.Error("Error ${response.code()}: ${response.message()}")
        }
    } catch (e: Exception) {
        Log.e(TAG, "❌ Excepción en la llamada API: ${e.message}")
        ApiResult.Error(e.message ?: "Error desconocido")
    }
} 