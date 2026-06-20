package com.example.bolaodapelada.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bolaodapelada.data.AppDatabase
import com.example.bolaodapelada.data.repository.UsuarioRepository
import com.example.bolaodapelada.databinding.ActivityRecuperarSenhaBinding
import kotlinx.coroutines.launch

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarSenhaBinding
    private lateinit var repository: UsuarioRepository
    private var emailEncontrado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        repository = UsuarioRepository(database.usuarioDao())

        binding.btnConfirmar.setOnClickListener {
            confirmarAcao()
        }
    }

    private fun confirmarAcao() {
        if (emailEncontrado == null) {
            buscarEmail()
        } else {
            redefinirSenha()
        }
    }

    private fun buscarEmail() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Digite seu email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val usuario = repository.buscarPorEmail(email)
            if (usuario != null) {
                emailEncontrado = email
                binding.tilNovaSenha.visibility = View.VISIBLE
                binding.btnConfirmar.text = "Redefinir Senha"
                Toast.makeText(this@RecuperarSenhaActivity, "Email encontrado! Digite sua nova senha", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@RecuperarSenhaActivity, "Email não cadastrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redefinirSenha() {
        val novaSenha = binding.etNovaSenha.text.toString().trim()

        if (novaSenha.isEmpty()) {
            Toast.makeText(this, "Digite a nova senha", Toast.LENGTH_SHORT).show()
            return
        }

        if (novaSenha.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val usuario = repository.buscarPorEmail(emailEncontrado!!)
            if (usuario != null) {
                val usuarioAtualizado = usuario.copy(senha = novaSenha)
                repository.atualizar(usuarioAtualizado)
                Toast.makeText(this@RecuperarSenhaActivity, "Senha redefinida com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}