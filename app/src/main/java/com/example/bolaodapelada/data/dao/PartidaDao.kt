package com.example.bolaodapelada.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bolaodapelada.data.entity.Partida
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidaDao {
    @Insert
    suspend fun inserir(partida: Partida): Long

    @Update
    suspend fun atualizar(partida: Partida)

    @Delete
    suspend fun excluir(partida: Partida)

    @Query("SELECT * FROM partidas WHERE usuarioId = :usuarioId AND status = 'FUTURA' ORDER BY data ASC")
    fun buscarFuturas(usuarioId: Long): Flow<List<Partida>>

    @Query("SELECT * FROM partidas WHERE usuarioId = :usuarioId AND status = 'PASSADA' ORDER BY data DESC")
    fun buscarPassadas(usuarioId: Long): Flow<List<Partida>>

    @Query("SELECT * FROM partidas WHERE usuarioId = :usuarioId")
    fun buscarTodas(usuarioId: Long): Flow<List<Partida>>
}