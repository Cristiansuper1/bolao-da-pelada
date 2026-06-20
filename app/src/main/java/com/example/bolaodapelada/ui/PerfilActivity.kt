package com.example.bolaodapelada.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bolaodapelada.data.AppDatabase
import com.example.bolaodapelada.data.repository.UsuarioRepository
import com.example.bolaodapelada.databinding.ActivityPerfilBinding
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var repository: UsuarioRepository
    private var usuarioId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        repository = UsuarioRepository(database.usuarioDao())

        usuarioId = intent.getLongExtra("USUARIO_ID", -1)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnSalvar.setOnClickListener {
            salvarAlteracoes()
        }

        binding.btnExcluirConta.setOnClickListener {
            confirmarExclusao()
        }

        carregarDadosUsuario()
    }

    private fun carregarDadosUsuario() {
        lifecycleScope.launch {
            val usuario = repository.buscarPorId(usuarioId)
            if (usuario != null) {
                binding.etPrimeiroNome.setText(usuario.primeiroNome)
                binding.etSegundoNome.setText(usuario.segundoNome)
                binding.etEmail.setText(usuario.email)
            } else {
                Toast.makeText(this@PerfilActivity, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun salvarAlteracoes() {
        val primeiroNome = binding.etPrimeiroNome.text.toString().trim()
        val segundoNome = binding.etSegundoNome.text.toString().trim()
        val novaSenha = binding.etNovaSenha.text.toString().trim()
        val confirmarSenha = binding.etConfirmarSenha.text.toString().trim()

        if (primeiroNome.isEmpty()) {
            Toast.makeText(this, "O primeiro nome não pode estar vazio", Toast.LENGTH_SHORT).show()
            return
        }

        if (novaSenha.isNotEmpty()) {
            if (novaSenha.length < 6) {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return
            }

            if (novaSenha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return
            }
        }

        lifecycleScope.launch {
            val usuario = repository.buscarPorId(usuarioId)
            if (usuario != null) {
                val usuarioAtualizado = if (novaSenha.isNotEmpty()) {
                    usuario.copy(
                        primeiroNome = primeiroNome,
                        segundoNome = segundoNome,
                        senha = novaSenha
                    )
                } else {
                    usuario.copy(
                        primeiroNome = primeiroNome,
                        segundoNome = segundoNome
                    )
                }

                repository.atualizar(usuarioAtualizado)
                Toast.makeText(this@PerfilActivity, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()

                binding.etNovaSenha.setText("")
                binding.etConfirmarSenha.setText("")
            }
        }
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Conta")
            .setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita e todos os seus dados serão perdidos.")
            .setPositiveButton("Sim, Excluir") { _, _ ->
                excluirConta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirConta() {
        lifecycleScope.launch {
            repository.excluir(usuarioId)
            Toast.makeText(this@PerfilActivity, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@PerfilActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}