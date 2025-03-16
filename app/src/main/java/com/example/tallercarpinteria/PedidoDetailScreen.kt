package com.example.tallercarpinteria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tallercarpinteria.api.Pedido
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoDetailScreen(
    pedido: Pedido,
    onBack: () -> Unit,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var pedidoEditado by remember { mutableStateOf(pedido) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val isUpdating by viewModel.isUpdating.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = { Text("Pedido #${pedido.id}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isEditing) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val success = viewModel.updatePedido(pedidoEditado)
                                        if (success) {
                                            isEditing = false
                                        } else {
                                            errorMessage = "Error al guardar los cambios"
                                            showErrorDialog = true
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Guardar")
                            }
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Información General",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (isEditing) {
                                OutlinedTextField(
                                    value = pedidoEditado.cliente_nombre,
                                    onValueChange = { pedidoEditado = pedidoEditado.copy(cliente_nombre = it) },
                                    label = { Text("Cliente") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                InfoRow("Cliente", pedidoEditado.cliente_nombre)
                            }
                        }
                    }
                }

                // Sección de Etapas y Fechas
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Etapas y Fechas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val etapas = listOf("medicion", "presupuesto", "materiales", "entrega")
                            etapas.forEach { etapa ->
                                EtapaConFecha(
                                    etapa = etapa,
                                    isCurrentEtapa = pedidoEditado.estado == etapa,
                                    fecha = when (etapa) {
                                        "medicion" -> pedidoEditado.fecha_medicion
                                        "presupuesto" -> pedidoEditado.fecha_presupuesto
                                        "materiales" -> pedidoEditado.fecha_materiales
                                        "entrega" -> pedidoEditado.fecha_entrega
                                        else -> ""
                                    },
                                    isEditing = isEditing,
                                    onEtapaSelected = { if (isEditing) pedidoEditado = pedidoEditado.copy(estado = etapa) },
                                    onFechaChanged = { nuevaFecha ->
                                        pedidoEditado = when (etapa) {
                                            "medicion" -> pedidoEditado.copy(fecha_medicion = nuevaFecha)
                                            "presupuesto" -> pedidoEditado.copy(fecha_presupuesto = nuevaFecha)
                                            "materiales" -> pedidoEditado.copy(fecha_materiales = nuevaFecha)
                                            "entrega" -> pedidoEditado.copy(fecha_entrega = nuevaFecha)
                                            else -> pedidoEditado
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Sección de Imagen
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Imagen del Pedido",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (selectedImageUri != null || pedidoEditado.imagen_url != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = selectedImageUri ?: pedidoEditado.imagen_url
                                    ),
                                    contentDescription = "Imagen del pedido",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            if (isEditing) {
                                Button(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (selectedImageUri != null) "Cambiar imagen" else "Agregar imagen")
                                }
                            }
                        }
                    }
                }

                // Sección de Notas
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Notas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (isEditing) {
                                OutlinedTextField(
                                    value = pedidoEditado.notas ?: "",
                                    onValueChange = { pedidoEditado = pedidoEditado.copy(notas = it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    label = { Text("Notas adicionales") }
                                )
                            } else {
                                Text(
                                    text = pedidoEditado.notas ?: "Sin notas",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun EtapaConFecha(
    etapa: String,
    isCurrentEtapa: Boolean,
    fecha: String?,
    isEditing: Boolean,
    onEtapaSelected: () -> Unit,
    onFechaChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEditing, onClick = onEtapaSelected)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (etapa) {
                "medicion" -> Icons.Default.Straighten
                "presupuesto" -> Icons.Default.Calculate
                "materiales" -> Icons.Default.Handyman
                "entrega" -> Icons.Default.LocalShipping
                else -> Icons.Default.Assignment
            },
            contentDescription = null,
            tint = if (isCurrentEtapa) Color(0xFF4CAF50) else Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (etapa) {
                    "medicion" -> "Medición"
                    "presupuesto" -> "Presupuesto"
                    "materiales" -> "Materiales"
                    "entrega" -> "Entrega"
                    else -> etapa
                }.replaceFirstChar { it.uppercase() }
            )
            if (isEditing) {
                OutlinedTextField(
                    value = fecha ?: "",
                    onValueChange = onFechaChanged,
                    label = { Text("Fecha") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (!fecha.isNullOrEmpty()) {
                Text(
                    text = fecha.substring(0, 10),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
} 