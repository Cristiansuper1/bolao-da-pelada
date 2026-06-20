package com.example.bolaodapelada.data.repository

import com.example.bolaodapelada.data.dao.UsuarioDao
import com.example.bolaodapelada.data.entity.Usuario

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun criarConta(usuario: Usuario): Long {
        return usuarioDao.inserir(usuario)
    }

    suspend fun login(email: String, senha: String): Usuario? {
        return usuarioDao.login(email, senha)
    }

    suspend fun buscarPorEmail(email: String): Usuario? {
        return usuarioDao.buscarPorEmail(email)
    }

    suspend fun buscarPorId(id: Long): Usuario? {
        return usuarioDao.buscarPorId(id)
    }

    suspend fun atualizar(usuario: Usuario) {
        usuarioDao.atualizar(usuario)
    }

    suspend fun excluir(id: Long) {
        usuarioDao.excluir(id)
    }
}