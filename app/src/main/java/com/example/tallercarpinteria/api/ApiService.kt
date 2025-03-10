package com.example.tallercarpinteria.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class Pedido(
    val id: Int,
    val cliente: String,
    val descripcion: String,
    val estado: String,
    val fecha_creacion: String,
    val plano: String? = null,
    val precio: Double? = null,
    val fecha_entrega: String? = null
)

interface ApiInterface {
    @POST("token/")
    suspend fun login(@Body credentials: Map<String, String>): Response<TokenResponse>

    @POST("token/refresh/")
    suspend fun refreshToken(@Body refreshToken: Map<String, String>): Response<TokenResponse>

    @GET("pedidos/")
    suspend fun getPedidos(@Header("Authorization") token: String): Response<List<Pedido>>
}

object ApiService {
    private const val BASE_URL = "http://127.0.0.1:8000/api/"  // localhost en Android
    private var accessToken: String? = null
    private var refreshToken: String? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ApiInterface::class.java)

    suspend fun login(username: String, password: String): Boolean {
        try {
            val response = api.login(mapOf(
                "username" to username,
                "password" to password
            ))

            if (response.isSuccessful) {
                response.body()?.let { tokens ->
                    accessToken = tokens.access
                    refreshToken = tokens.refresh
                    return true
                }
            } else {
                println("Error en login: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("Exception en login: ${e.message}")
            e.printStackTrace()
        }
        return false
    }

    suspend fun getPedidos(): List<Pedido> {
        try {
            accessToken?.let { token ->
                val response = api.getPedidos("Bearer $token")
                if (response.isSuccessful) {
                    return response.body() ?: emptyList()
                } else if (response.code() == 401) {
                    println("Token expirado, intentando refrescar...")
                    // Token expirado, intentar refrescar
                    if (refreshAccessToken()) {
                        // Reintentar con el nuevo token
                        accessToken?.let { newToken ->
                            val newResponse = api.getPedidos("Bearer $newToken")
                            if (newResponse.isSuccessful) {
                                return newResponse.body() ?: emptyList()
                            } else {
                                println("Error en getPedidos después de refresh: ${newResponse.code()} - ${newResponse.errorBody()?.string()}")
                            }
                        }
                    }
                } else {
                    println("Error en getPedidos: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            println("Exception en getPedidos: ${e.message}")
            e.printStackTrace()
        }
        return emptyList()
    }

    private suspend fun refreshAccessToken(): Boolean {
        try {
            refreshToken?.let { refresh ->
                val response = api.refreshToken(mapOf("refresh" to refresh))
                if (response.isSuccessful) {
                    response.body()?.let { tokens ->
                        accessToken = tokens.access
                        return true
                    }
                } else {
                    println("Error en refreshToken: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            println("Exception en refreshToken: ${e.message}")
            e.printStackTrace()
        }
        return false
    }
} 