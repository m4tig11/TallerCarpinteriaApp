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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled. *
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Note
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked

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
    var selectedPedido by remember { mutableStateOf<Pedido?>(null) }
    val error by viewModel.error.collectAsState()
    
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

        if (error != null) {
            Text(
                text = error ?: "",
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.filterByEstado("solicitado") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentFilter == "solicitado") Color(0xFF2196F3) else Color.White,
                    contentColor = if (viewModel.currentFilter == "solicitado") Color.White else Color(0xFF2196F3)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (viewModel.currentFilter == "solicitado") 4.dp else 0.dp
                ),
                border = BorderStroke(1.dp, Color(0xFF2196F3))
            ) {
                Text("Solicitados")
            }
            Button(
                onClick = { viewModel.filterByEstado("en_proceso") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentFilter == "en_proceso") Color(0xFF2196F3) else Color.White,
                    contentColor = if (viewModel.currentFilter == "en_proceso") Color.White else Color(0xFF2196F3)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (viewModel.currentFilter == "en_proceso") 4.dp else 0.dp
                ),
                border = BorderStroke(1.dp, Color(0xFF2196F3))
            ) {
                Text("En proceso")
            }
            Button(
                onClick = { viewModel.filterByEstado("terminado") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentFilter == "terminado") Color(0xFF2196F3) else Color.White,
                    contentColor = if (viewModel.currentFilter == "terminado") Color.White else Color(0xFF2196F3)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (viewModel.currentFilter == "terminado") 4.dp else 0.dp
                ),
                border = BorderStroke(1.dp, Color(0xFF2196F3))
            ) {
                Text("Terminados")
            }
        }

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredPedidos = pedidos.filter { pedido ->
                    val matchesSearch = if (searchText.isEmpty()) {
                        true
                    } else {
                        pedido.cliente_nombre.contains(searchText, ignoreCase = true)
                    }
                    
                    val matchesFilter = if (viewModel.currentFilter.isEmpty()) {
                        true
                    } else {
                        pedido.estado.equals(viewModel.currentFilter, ignoreCase = true)
                    }
                    
                    matchesSearch && matchesFilter
                }

                if (filteredPedidos.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = if (pedidos.isEmpty()) 
                                "No hay pedidos disponibles" 
                            else 
                                "No hay pedidos que coincidan con el filtro",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(filteredPedidos) { pedido ->
                        PedidoCard(
                            pedido = pedido,
                            onClick = { selectedPedido = pedido }
                        )
                    }
                }
            }
        }

        // Debug info
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Pedidos totales: ${pedidos.size}",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "Filtro actual: ${if(viewModel.currentFilter.isEmpty()) "Ninguno" else viewModel.currentFilter}",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "Búsqueda: ${if(searchText.isEmpty()) "Ninguna" else searchText}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    // Reemplazar el Dialog por la nueva pantalla de detalles
    if (selectedPedido != null) {
        PedidoDetailScreen(
            pedido = selectedPedido!!,
            onBack = { selectedPedido = null },
            viewModel = viewModel
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EtapaItem(etapa: String, isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isCompleted) Color(0xFF4CAF50) else Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = etapa)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoCard(pedido: Pedido, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ID Badge arriba a la izquierda
            Surface(
                modifier = Modifier.wrapContentSize(),
                shape = CircleShape,
                color = Color(0xFFEEEEEE)
            ) {
                Text(
                    text = "#${pedido.id}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            // Nombre del cliente centrado
            Text(
                text = pedido.cliente_nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Icono centrado
            Icon(
                imageVector = when (pedido.estado.lowercase()) {
                    "medicion" -> Icons.Default.Straighten // Regla/medición
                    "materiales" -> Icons.Default.Handyman // Herramientas/materiales
                    "presupuesto" -> Icons.Default.Calculate // Calculadora
                    "entrega" -> Icons.Default.LocalShipping // Camión de entrega
                    else -> Icons.Default.Assignment // Documento por defecto
                },
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterHorizontally),
                tint = when (pedido.estado.lowercase()) {
                    "medicion" -> Color(0xFFFFB300) // Amarillo más oscuro
                    "materiales" -> Color(0xFFBF360C) // Marrón/naranja para materiales
                    "presupuesto" -> Color(0xFF1976D2) // Azul para presupuesto
                    "entrega" -> Color(0xFF2E7D32) // Verde oscuro
                    else -> Color(0xFF757575) // Gris por defecto
                }
            )

            // Etapa y fecha en una superficie coloreada
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = when (pedido.estado.lowercase()) {
                    "medicion" -> Color(0xFFFFF176) // Amarillo para medición
                    "materiales" -> Color(0xFFFFF176) // Amarillo para materiales
                    "presupuesto" -> Color(0xFFFFF176) // Amarillo para presupuesto
                    "entrega" -> Color(0xFF81C784) // Verde para entrega
                    else -> Color(0xFFEF5350) // Rojo para solicitado
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pedido.estado.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Text(
                        text = pedido.fecha_medicion.substring(5, 10).replace('-', '/'),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
