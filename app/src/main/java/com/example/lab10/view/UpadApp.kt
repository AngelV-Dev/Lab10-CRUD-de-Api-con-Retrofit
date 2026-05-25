package com.example.lab10.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab10.data.RutinaApiService // Asegúrate de haber creado este archivo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun SeriesApp() { // Puedes mantener el nombre o cambiarlo a UpadApp
    // URL DE TU MOCKAPI
    val urlBase = "https://6a13838c6c7db8aac0531c7e.mockapi.io/api/v1/"

    val retrofit = Retrofit.Builder().baseUrl(urlBase)
        .addConverterFactory(GsonConverterFactory.create()).build()
    val servicio = retrofit.create(RutinaApiService::class.java)
    val navController = rememberNavController()

    Scaffold(
        topBar =    { BarraSuperior() },
        bottomBar = { BarraInferior(navController) },
        floatingActionButton = { BotonFAB(navController, servicio) },
        content =   { paddingValues -> Contenido(paddingValues, navController, servicio) }
    )
}

@Composable
fun BotonFAB(navController: NavHostController, servicio: RutinaApiService) {
    val cbeState by navController.currentBackStackEntryAsState()
    val rutaActual = cbeState?.destination?.route
    if (rutaActual == "rutinas") {
        FloatingActionButton(
            containerColor = Color(0xFF1565C0),
            contentColor = Color.White,
            onClick = { navController.navigate("rutinaNuevo") }
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "UPAD - RUTINAS",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF1565C0) // Azul profesional
        )
    )
}

@Composable
fun BarraInferior(navController: NavHostController) {
    NavigationBar(containerColor = Color.LightGray) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = navController.currentDestination?.route == "inicio",
            onClick = { navController.navigate("inicio") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Rutinas") },
            label = { Text("Rutinas") },
            selected = navController.currentDestination?.route == "rutinas",
            onClick = { navController.navigate("rutinas") }
        )
    }
}

@Composable
fun Contenido(
    pv: PaddingValues,
    navController: NavHostController,
    servicio: RutinaApiService
) {
    Box(modifier = Modifier.fillMaxSize().padding(pv)) {
        NavHost(
            navController = navController,
            startDestination = "inicio"
        ) {
            composable("inicio") { ScreenInicio() }
            composable("rutinas") { ContenidoRutinasListado(navController, servicio) }
            composable("rutinaNuevo") {
                ContenidoRutinaEditar(navController, servicio, "0" )
            }
            composable("rutinaVer/{id}", arguments = listOf(
                navArgument("id") { type = NavType.StringType} )
            ) {
                ContenidoRutinaEditar(navController, servicio, it.arguments!!.getString("id")!!)
            }
            composable("rutinaDel/{id}", arguments = listOf(
                navArgument("id") { type = NavType.StringType} )
            ) {
                ContenidoRutinaEliminar(navController, servicio, it.arguments!!.getString("id")!!)
            }
        }
    }
}
