package com.example.bolaodapelada.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bolaodapelada.MainActivity
import com.example.bolaodapelada.data.AppDatabase
import com.example.bolaodapelada.data.repository.UsuarioRepository
import com.example.bolaodapelada.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        repository = UsuarioRepository(database.usuarioDao())

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            realizarLogin()
        }

        binding.tvRegistrar.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        binding.tvEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }
    }

    private fun realizarLogin() {
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString().trim()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val usuario = repository.login(email, senha)
            if (usuario != null) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("USUARIO_ID", usuario.id)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}