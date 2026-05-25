package com.example.lab10.data

import com.google.gson.annotations.SerializedName

data class RutinaModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("hora")
    val hora: String,
    @SerializedName("id_pictograma")
    val id_pictograma: Long
)