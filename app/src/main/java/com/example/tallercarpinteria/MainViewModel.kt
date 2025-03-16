package com.example.tallercarpinteria

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallercarpinteria.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel() {
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    var isLoading by mutableStateOf(false)
        private set

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    var currentFilter by mutableStateOf("")
        private set

    private val apiService = ApiService.getInstance()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    init {
        Log.d("MainViewModel", "🚀 ViewModel inicializado")
        loadPedidos()
    }

    fun loadPedidos() {
        viewModelScope.launch {
            try {
                isLoading = true
                _error.value = null
                Log.d("MainViewModel", "📥 Iniciando carga de pedidos...")

                if (!ApiService.isAuthenticated()) {
                    Log.d("MainViewModel", "🔐 Iniciando proceso de login...")
                    val loginResult = safeApiCall {
                        apiService.login(LoginRequest(
                            username = "matias",
                            password = "matigreco11"
                        )).also {
                            Log.d("MainViewModel", "📡 Enviando solicitud de login...")
                        }
                    }

                    when (loginResult) {
                        is ApiResult.Success -> {
                            Log.d("MainViewModel", "✅ Login exitoso")
                            ApiService.setToken(loginResult.data.access)
                            Log.d("MainViewModel", "🔑 Token guardado exitosamente")
                        }
                        is ApiResult.Error -> {
                            _error.value = "Error de autenticación: ${loginResult.message}"
                            Log.e("MainViewModel", "❌ Error de login: ${_error.value}")
                            isLoading = false
                            return@launch
                        }
                        is ApiResult.Loading -> {
                            Log.d("MainViewModel", "⏳ Procesando login...")
                        }
                    }
                }

                Log.d("MainViewModel", "📥 Obteniendo lista de pedidos...")
                val result = safeApiCall {
                    apiService.getPedidos(ApiService.getAuthHeader())
                }

                when (result) {
                    is ApiResult.Success -> {
                        val pedidosRecibidos = result.data
                        Log.d("MainViewModel", "📦 Número de pedidos recibidos: ${pedidosRecibidos.size}")
                        
                        if (pedidosRecibidos.isEmpty()) {
                            Log.d("MainViewModel", "⚠️ La lista de pedidos está vacía")
                        } else {
                            Log.d("MainViewModel", "✅ Pedidos recibidos correctamente")
                            pedidosRecibidos.forEach { pedido ->
                                Log.d("MainViewModel", """
                                    📝 Pedido #${pedido.id}:
                                    Cliente: ${pedido.cliente_nombre ?: "Sin cliente"}
                                    Estado: ${pedido.estado}
                                    Fecha: ${pedido.fecha_medicion}
                                """.trimIndent())
                            }
                        }
                        
                        _pedidos.value = pedidosRecibidos
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                        Log.e("MainViewModel", "❌ Error al cargar pedidos: ${_error.value}")
                    }
                    is ApiResult.Loading -> {
                        Log.d("MainViewModel", "⏳ Cargando pedidos...")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("MainViewModel", "❌ Excepción al cargar pedidos: ${e.message}")
            } finally {
                isLoading = false
                Log.d("MainViewModel", "🏁 Proceso de carga finalizado")
            }
        }
    }

    fun filterByEstado(estado: String) {
        currentFilter = estado
        Log.d("MainViewModel", "🔍 Filtro aplicado: $estado")
    }

    suspend fun updatePedido(pedido: Pedido, imageFile: File? = null): Boolean {
        return try {
            _isUpdating.value = true
            Log.d("MainViewModel", "Actualizando pedido: $pedido")
            
            if (!ApiService.isAuthenticated()) {
                Log.d("MainViewModel", "No autenticado, intentando login...")
                val loginResult = safeApiCall {
                    apiService.login(LoginRequest(
                        username = "matias",
                        password = "matigreco11"
                    ))
                }

                when (loginResult) {
                    is ApiResult.Success -> {
                        Log.d("MainViewModel", "Login exitoso para actualización")
                        ApiService.setToken(loginResult.data.access)
                    }
                    is ApiResult.Error -> {
                        _error.value = "Error de autenticación: ${loginResult.message}"
                        return false
                    }
                    is ApiResult.Loading -> {}
                }
            }

            val pedidoUpdate = PedidoUpdate(
                cliente_nombre = pedido.cliente_nombre,
                estado = pedido.estado,
                fecha_medicion = pedido.fecha_medicion,
                fecha_presupuesto = pedido.fecha_presupuesto,
                fecha_materiales = pedido.fecha_materiales,
                fecha_entrega = pedido.fecha_entrega,
                notas = pedido.notas,
                presupuesto = pedido.presupuesto,
                direccion = pedido.direccion,
                telefono = pedido.telefono,
                email = pedido.email,
                detalles_medicion = pedido.detalles_medicion,
                lista_materiales = pedido.lista_materiales,
                observaciones = pedido.observaciones,
                plano = pedido.plano
            )

            Log.d("MainViewModel", "📦 Enviando datos: $pedidoUpdate")

            val pedidoData = ApiService.createPedidoRequestBody(pedidoUpdate)
            val imagePart = imageFile?.let { 
                Log.d("MainViewModel", "📸 Preparando imagen para envío: ${it.absolutePath}")
                ApiService.createMultipartImage(it) 
            }

            Log.d("MainViewModel", "🚀 Enviando pedido con ${if(imagePart != null) "imagen" else "sin imagen"}")

            val result = safeApiCall {
                apiService.updatePedidoWithImage(
                    token = ApiService.getAuthHeader(),
                    pedidoId = pedido.id,
                    pedidoData = pedidoData,
                    imagen = imagePart
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    Log.d("MainViewModel", "✅ Pedido actualizado exitosamente")
                    val updatedPedidos = _pedidos.value.map { 
                        if (it.id == pedido.id) result.data else it 
                    }
                    _pedidos.value = updatedPedidos
                    _error.value = null
                    true
                }
                is ApiResult.Error -> {
                    Log.e("MainViewModel", "❌ Error al actualizar pedido: ${result.message}")
                    _error.value = "Error al actualizar el pedido: ${result.message}"
                    false
                }
                is ApiResult.Loading -> false
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "❌ Error al actualizar pedido", e)
            _error.value = "Error al actualizar el pedido: ${e.message}"
            false
        } finally {
            _isUpdating.value = false
        }
    }
} 