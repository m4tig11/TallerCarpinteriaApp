package com.example.tallercarpinteria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallercarpinteria.api.Pedido
import com.example.tallercarpinteria.MainViewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TallerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TallerScreen() {
    val viewModel: MainViewModel = viewModel()
    var searchText by remember { mutableStateOf("") }
    val pedidos by viewModel.pedidos.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Taller", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    IconButton(onClick = { viewModel.loadPedidos() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar")
                    }
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2196F3),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )

        if (viewModel.error != null) {
            Text(
                text = viewModel.error ?: "",
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Cliente") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = { viewModel.filterByEstado("solicitado") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentFilter == "solicitado") Color(0xFF2196F3) else Color.LightGray
                )
            ) {
                Text(
                    "Solicitados",
                    color = if (viewModel.currentFilter == "solicitado") Color.White else Color.Black
                )
            }
            Button(
                onClick = { viewModel.filterByEstado("en_proceso") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentFilter == "en_proceso") Color(0xFF2196F3) else Color.LightGray
                )
            ) {
                Text(
                    "En proceso",
                    color = if (viewModel.currentFilter == "en_proceso") Color.White else Color.Black
                )
            }
            Button(
                onClick = { viewModel.filterByEstado("terminado") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentFilter == "terminado") Color(0xFF2196F3) else Color.LightGray
                )
            ) {
                Text(
                    "Terminados",
                    color = if (viewModel.currentFilter == "terminado") Color.White else Color.Black
                )
            }
        }

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (pedidos.isEmpty()) {
            Text(
                text = "No hay pedidos disponibles",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(pedidos.filter { 
                    it.cliente.contains(searchText, ignoreCase = true) &&
                    (viewModel.currentFilter.isEmpty() || it.estado == viewModel.currentFilter)
                }) { pedido ->
                    PedidoCard(pedido = pedido)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoCard(pedido: Pedido) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Cliente: ${pedido.cliente}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Descripción: ${pedido.descripcion}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estado: ${pedido.estado}",
                color = when (pedido.estado) {
                    "solicitado" -> Color(0xFF2196F3)
                    "en_proceso" -> Color.Gray
                    "terminado" -> Color.Green
                    else -> Color.Black
                }
            )
            Text(text = "Fecha: ${pedido.fecha_creacion}")
            pedido.precio?.let { precio ->
                Text(text = "Precio: $${precio}")
            }
            pedido.fecha_entrega?.let { fecha ->
                Text(text = "Fecha de entrega: $fecha")
            }
        }
    }
}
