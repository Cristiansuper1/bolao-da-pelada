package com.example.bolaodapelada.data.repository

import com.example.bolaodapelada.data.dao.PartidaDao
import com.example.bolaodapelada.data.entity.Partida
import kotlinx.coroutines.flow.Flow

class PartidaRepository(private val partidaDao: PartidaDao) {

    suspend fun adicionar(partida: Partida): Long {
        return partidaDao.inserir(partida)
    }

    suspend fun atualizar(partida: Partida) {
        partidaDao.atualizar(partida)
    }

    suspend fun excluir(partida: Partida) {
        partidaDao.excluir(partida)
    }

    fun buscarFuturas(usuarioId: Long): Flow<List<Partida>> {
        return partidaDao.buscarFuturas(usuarioId)
    }

    fun buscarPassadas(usuarioId: Long): Flow<List<Partida>> {
        return partidaDao.buscarPassadas(usuarioId)
    }

    fun buscarTodas(usuarioId: Long): Flow<List<Partida>> {
        return partidaDao.buscarTodas(usuarioId)
    }
}