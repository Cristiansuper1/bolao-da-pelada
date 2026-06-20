package com.example.bolaodapelada.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bolaodapelada.data.AppDatabase
import com.example.bolaodapelada.data.entity.Usuario
import com.example.bolaodapelada.data.repository.UsuarioRepository
import com.example.bolaodapelada.databinding.ActivityCadastroBinding
import kotlinx.coroutines.launch

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var repository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        repository = UsuarioRepository(database.usuarioDao())

        binding.btnCadastrar.setOnClickListener {
            cadastrarUsuario()
        }
    }

    private fun cadastrarUsuario() {
        val primeiroNome = binding.etPrimeiroNome.text.toString().trim()
        val segundoNome = binding.etSegundoNome.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString().trim()
        val confirmarSenha = binding.etConfirmarSenha.text.toString().trim()

        if (primeiroNome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha != confirmarSenha) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val usuarioExistente = repository.buscarPorEmail(email)
            if (usuarioExistente != null) {
                Toast.makeText(this@CadastroActivity, "Email já cadastrado", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val usuario = Usuario(
                primeiroNome = primeiroNome,
                segundoNome = segundoNome,
                email = email,
                senha = senha
            )

            val id = repository.criarConta(usuario)
            if (id > 0) {
                Toast.makeText(this@CadastroActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@CadastroActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@CadastroActivity, "Erro ao cadastrar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}