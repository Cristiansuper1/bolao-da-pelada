package com.example.bolaodapelada.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partidas")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val timeA: String,
    val timeB: String,
    val golsA: Int? = null,
    val golsB: Int? = null,
    val previsaoGolsA: Int? = null,
    val previsaoGolsB: Int? = null,
    val imagemUriTimeA: String? = null,
    val imagemUriTimeB: String? = null,
    val observacoes: String? = null,
    val data: Long,
    val status: String
)