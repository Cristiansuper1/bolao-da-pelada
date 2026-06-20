package com.example.bolaodapelada.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.bolaodapelada.data.AppDatabase
import com.example.bolaodapelada.data.repository.PartidaRepository
import com.example.bolaodapelada.data.repository.UsuarioRepository
import com.example.bolaodapelada.databinding.ActivityConfiguracoesBinding
import com.example.bolaodapelada.util.PdfGenerator
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var partidaRepository: PartidaRepository
    private var usuarioId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        usuarioRepository = UsuarioRepository(database.usuarioDao())
        partidaRepository = PartidaRepository(database.partidaDao())

        usuarioId = intent.getLongExtra("USUARIO_ID", -1)

        binding.toolbarConfig.setNavigationOnClickListener {
            finish()
        }

        binding.cardPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
        }

        binding.cardAcessibilidade.setOnClickListener {
            Toast.makeText(this, "Acessibilidade será implementada em versões futuras", Toast.LENGTH_LONG).show()
        }

        binding.cardExportar.setOnClickListener {
            exportarRelatorio()
        }

        binding.cardSair.setOnClickListener {
            confirmarSaida()
        }
    }

    private fun exportarRelatorio() {
        lifecycleScope.launch {
            val usuario = usuarioRepository.buscarPorId(usuarioId)
            val nomeUsuario = if (usuario != null) {
                "${usuario.primeiroNome} ${usuario.segundoNome}"
            } else {
                "Usuário"
            }

            val todasAsPartidas = partidaRepository.buscarTodas(usuarioId).first()

            if (todasAsPartidas.isEmpty()) {
                Toast.makeText(this@ConfiguracoesActivity, "Nenhuma partida cadastrada para exportar", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val arquivo = PdfGenerator.gerarRelatorio(this@ConfiguracoesActivity, todasAsPartidas, nomeUsuario)

            if (arquivo != null) {
                compartilharPdf(arquivo)
            } else {
                Toast.makeText(this@ConfiguracoesActivity, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compartilharPdf(arquivo: java.io.File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            arquivo
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Bolão da Pelada")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Compartilhar relatório via"))
    }

    private fun confirmarSaida() {
        AlertDialog.Builder(this)
            .setTitle("Sair")
            .setMessage("Deseja realmente sair do aplicativo?")
            .setPositiveButton("Sim") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Não", null)
            .show()
    }
}