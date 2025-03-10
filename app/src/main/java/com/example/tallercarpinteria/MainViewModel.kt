package com.example.tallercarpinteria

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallercarpinteria.api.ApiService
import com.example.tallercarpinteria.api.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var currentFilter by mutableStateOf("")
        private set

    // Credenciales de la API
    private val username = "matias"  // Cambia esto por tu usuario
    private val password = "matigreco11"  // Cambia esto por tu contraseña

    init {
        loadPedidos()
    }

    fun loadPedidos() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                // Primero intentamos hacer login
                val loginSuccess = ApiService.login(username, password)
                if (loginSuccess) {
                    // Si el login es exitoso, cargamos los pedidos
                    val pedidosList = ApiService.getPedidos()
                    if (pedidosList.isEmpty()) {
                        error = "No se encontraron pedidos"
                    } else {
                        _pedidos.value = pedidosList
                        error = null
                    }
                } else {
                    error = "Error en el inicio de sesión. Verifica tus credenciales."
                }
            } catch (e: Exception) {
                error = "Error de conexión: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun filterByEstado(estado: String) {
        currentFilter = if (currentFilter == estado) "" else estado
    }
} 