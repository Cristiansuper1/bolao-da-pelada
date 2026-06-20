package com.example.bolaodapelada.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bolaodapelada.data.entity.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun inserir(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email AND senha = :senha")
    suspend fun login(email: String, senha: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE email = :email")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Long): Usuario?

    @Update
    suspend fun atualizar(usuario: Usuario)

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun excluir(id: Long)
}