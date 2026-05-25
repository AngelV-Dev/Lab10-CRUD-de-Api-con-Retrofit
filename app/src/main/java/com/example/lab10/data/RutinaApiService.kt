package com.example.lab10.data

import retrofit2.Response
import retrofit2.http.*

interface RutinaApiService {
    @GET("rutinas")
    suspend fun selectRutinas(): ArrayList<RutinaModel>

    @GET("rutinas/{id}")
    suspend fun selectRutina(@Path("id") id: String): Response<RutinaModel>

    @POST("rutinas")
    suspend fun insertRutina(@Body rutina: RutinaModel): Response<RutinaModel>

    @PUT("rutinas/{id}")
    suspend fun updateRutina(@Path("id") id: String, @Body rutina: RutinaModel): Response<RutinaModel>

    @DELETE("rutinas/{id}")
    suspend fun deleteRutina(@Path("id") id: String): Response<Unit>
}