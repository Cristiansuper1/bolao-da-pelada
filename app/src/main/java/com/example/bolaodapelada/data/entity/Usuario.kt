package com.example.bolaodapelada.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val primeiroNome: String,
    val segundoNome: String,
    val email: String,
    val senha: String
)