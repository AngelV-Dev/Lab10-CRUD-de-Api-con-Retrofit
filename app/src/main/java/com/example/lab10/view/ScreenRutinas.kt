package com.example.lab10.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.lab10.data.RutinaApiService
import com.example.lab10.data.RutinaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

// --- Función auxiliar para buscar en ARASAAC sin modificar Retrofit ---
suspend fun buscarPictogramasEnArasaac(palabra: String): List<Long> {
    return withContext(Dispatchers.IO) {
        try {
            // Llama a la API de búsqueda de ARASAAC
            val url = "https://api.arasaac.org/api/pictograms/es/search/${palabra.trim()}"
            val response = URL(url).readText()
            val jsonArray = JSONArray(response)
            val ids = mutableListOf<Long>()

            // Extrae los IDs de los resultados (limitamos a 10 para no saturar la pantalla)
            val limit = if (jsonArray.length() > 10) 10 else jsonArray.length()
            for (i in 0 until limit) {
                ids.add(jsonArray.getJSONObject(i).getLong("_id"))
            }
            ids
        } catch (e: Exception) {
            emptyList() // Si hay error o no encuentra nada, devuelve lista vacía
        }
    }
}

@Composable
fun ContenidoRutinasListado(navController: NavHostController, servicio: RutinaApiService) {
    val listaRutinas: SnapshotStateList<RutinaModel> = remember { mutableStateListOf() }

    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectRutinas()
            listaRutinas.clear()
            listaRutinas.addAll(listado)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error al cargar rutinas: ${e.message}")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(listaRutinas) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen real de ARASAAC
                    val urlPictograma = "https://static.arasaac.org/pictograms/${item.id_pictograma}/${item.id_pictograma}_300.png"

                    AsyncImage(
                        model = urlPictograma,
                        contentDescription = "Pictograma de rutina",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(4.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.titulo,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Text(
                            text = "${item.hora} - ${item.descripcion}",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }

                    IconButton(onClick = { navController.navigate("rutinaVer/${item.id}") }) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Editar", tint = Color(0xFF1565C0))
                    }
                    IconButton(onClick = { navController.navigate("rutinaDel/${item.id}") }) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoRutinaEditar(navController: NavHostController, servicio: RutinaApiService, pid: String = "0") {
    var id by remember { mutableStateOf(pid) }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var idPictograma by remember { mutableLongStateOf(0L) }
    var grabar by remember { mutableStateOf(false) }

    // Estados para el buscador
    var palabraBusqueda by remember { mutableStateOf("") }
    var resultadosBusqueda by remember { mutableStateOf<List<Long>>(emptyList()) }
    var buscando by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (id != "0") {
        LaunchedEffect(Unit) {
            val response = servicio.selectRutina(id)
            delay(100)
            if (response.isSuccessful) {
                val obj = response.body()
                titulo = obj?.titulo ?: ""
                descripcion = obj?.descripcion ?: ""
                hora = obj?.hora ?: ""
                idPictograma = obj?.id_pictograma ?: 0L
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (id == "0") "Nueva Rutina Visual" else "Editar Rutina",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )

        // --- SECCIÓN DEL BUSCADOR ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Buscador de Pictogramas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = palabraBusqueda,
                        onValueChange = { palabraBusqueda = it },
                        placeholder = { Text("Ej: manzana, dormir...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (palabraBusqueda.isNotBlank()) {
                                buscando = true
                                coroutineScope.launch {
                                    resultadosBusqueda = buscarPictogramasEnArasaac(palabraBusqueda)
                                    buscando = false
                                }
                            }
                        },
                        modifier = Modifier.background(Color(0xFF1565C0), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = Color.White)
                    }
                }

                // Mostrar resultados de búsqueda
                if (buscando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
                } else if (resultadosBusqueda.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(resultadosBusqueda) { idPicto ->
                            AsyncImage(
                                model = "https://static.arasaac.org/pictograms/${idPicto}/${idPicto}_300.png",
                                contentDescription = "Resultado",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .clickable {
                                        idPictograma = idPicto
                                        resultadosBusqueda = emptyList() // Limpiar tras seleccionar
                                        if (titulo.isEmpty()) titulo = palabraBusqueda.capitalize()
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Vista previa del pictograma seleccionado
        if (idPictograma != 0L) {
            Text("Pictograma Seleccionado:", fontSize = 12.sp, color = Color.Gray)
            AsyncImage(
                model = "https://static.arasaac.org/pictograms/${idPictograma}/${idPictograma}_300.png",
                contentDescription = "Vista previa",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(8.dp)
            )
        }

        // --- FORMULARIO ---
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("¿Qué haremos? (Título)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción breve") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = hora,
            onValueChange = { hora = it },
            label = { Text("Hora (Ej: 08:00 AM)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { grabar = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            enabled = idPictograma != 0L && titulo.isNotEmpty()
        ) {
            Text("Guardar Rutina", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (grabar) {
        val objRutina = RutinaModel(id, titulo, descripcion, hora, idPictograma)

        LaunchedEffect(Unit) {
            if (id == "0") {
                servicio.insertRutina(objRutina)
            } else {
                servicio.updateRutina(id, objRutina)
            }
            grabar = false
            navController.navigate("rutinas") {
                popUpTo("rutinas") { inclusive = true }
            }
        }
    }
}

@Composable
fun ContenidoRutinaEliminar(navController: NavHostController, servicio: RutinaApiService, id: String) {
    var showDialog by remember { mutableStateOf(true) }
    var borrar by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFD32F2F)) },
            title = { Text(text = "Eliminar Rutina") },
            text = { Text("¿Estás seguro de que deseas quitar esta rutina de la agenda?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        borrar = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Mantener rutina")
                }
            }
        )
    }

    if (borrar) {
        LaunchedEffect(Unit) {
            servicio.deleteRutina(id)
            borrar = false
            navController.navigate("rutinas") {
                popUpTo("rutinas") { inclusive = true }
            }
        }
    }
}