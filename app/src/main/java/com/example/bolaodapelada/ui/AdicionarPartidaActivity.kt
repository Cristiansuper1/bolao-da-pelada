package com.example.bolaodapelada.ui

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bolaodapelada.data.AppDatabase
import com.example.bolaodapelada.data.entity.Partida
import com.example.bolaodapelada.data.repository.PartidaRepository
import com.example.bolaodapelada.databinding.ActivityAdicionarPartidaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AdicionarPartidaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarPartidaBinding
    private lateinit var repository: PartidaRepository
    private var usuarioId: Long = -1
    private var partidaId: Long = -1
    private var imagemPathTimeA: String? = null
    private var imagemPathTimeB: String? = null
    private var dataSelecionada: Long = 0
    private var isEdicao = false

    private val imagePickerLauncherA = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val caminho = copiarImagemParaInterno(it)
                if (caminho != null) {
                    imagemPathTimeA = caminho
                    binding.ivImagemTimeA.setImageURI(Uri.fromFile(File(caminho)))
                    binding.ivImagemTimeA.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@AdicionarPartidaActivity, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val imagePickerLauncherB = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val caminho = copiarImagemParaInterno(it)
                if (caminho != null) {
                    imagemPathTimeB = caminho
                    binding.ivImagemTimeB.setImageURI(Uri.fromFile(File(caminho)))
                    binding.ivImagemTimeB.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@AdicionarPartidaActivity, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        repository = PartidaRepository(database.partidaDao())

        usuarioId = intent.getLongExtra("USUARIO_ID", -1)
        partidaId = intent.getLongExtra("PARTIDA_ID", -1)
        isEdicao = partidaId != -1L

        if (isEdicao) {
            binding.toolbar.title = "Editar Partida"
            carregarPartida()
        }

        setupListeners()
        setupValidacaoTempoReal()
    }

    private suspend fun copiarImagemParaInterno(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
                val nomeArquivo = "img_${UUID.randomUUID()}.jpg"
                val arquivoDestino = File(filesDir, nomeArquivo)
                FileOutputStream(arquivoDestino).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                arquivoDestino.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.etData.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnSelecionarImagemA.setOnClickListener {
            imagePickerLauncherA.launch("image/*")
        }

        binding.btnSelecionarImagemB.setOnClickListener {
            imagePickerLauncherB.launch("image/*")
        }

        binding.btnSalvar.setOnClickListener {
            salvarPartida()
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            dataSelecionada = calendar.timeInMillis

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.etData.setText(dateFormat.format(calendar.time))

            val hoje = Calendar.getInstance()
            hoje.set(Calendar.HOUR_OF_DAY, 23)
            hoje.set(Calendar.MINUTE, 59)
            hoje.set(Calendar.SECOND, 59)

            if (calendar.before(hoje)) {
                binding.tvResultadoInfo.text = "Jogo já aconteceu - preencha o resultado"
                binding.layoutResultadoReal.alpha = 1f
                binding.etGolsA.isEnabled = true
                binding.etGolsB.isEnabled = true
            } else {
                binding.tvResultadoInfo.text = "Jogo ainda não aconteceu - apenas previsão"
                binding.layoutResultadoReal.alpha = 0.5f
                binding.etGolsA.isEnabled = false
                binding.etGolsB.isEnabled = false
                binding.etGolsA.text?.clear()
                binding.etGolsB.text?.clear()
            }
        }, year, month, day)

        datePicker.show()
    }

    private fun salvarPartida() {
        val timeA = binding.etTimeA.text.toString().trim()
        val timeB = binding.etTimeB.text.toString().trim()
        val golsAText = binding.etGolsA.text.toString().trim()
        val golsBText = binding.etGolsB.text.toString().trim()
        val previsaoAText = binding.etPrevisaoA.text.toString().trim()
        val previsaoBText = binding.etPrevisaoB.text.toString().trim()
        val observacoes = binding.etObservacoes.text.toString().trim()

        if (timeA.isEmpty() || timeB.isEmpty()) {
            Toast.makeText(this, "Preencha os nomes dos times", Toast.LENGTH_SHORT).show()
            return
        }

        if (dataSelecionada == 0L) {
            Toast.makeText(this, "Selecione a data do jogo", Toast.LENGTH_SHORT).show()
            return
        }

        if (previsaoAText.isEmpty() || previsaoBText.isEmpty()) {
            Toast.makeText(this, "Preencha sua previsão", Toast.LENGTH_SHORT).show()
            return
        }

        val golsA = if (golsAText.isNotEmpty()) golsAText.toIntOrNull() else null
        val golsB = if (golsBText.isNotEmpty()) golsBText.toIntOrNull() else null
        val previsaoA = previsaoAText.toIntOrNull()
        val previsaoB = previsaoBText.toIntOrNull()

        val hoje = System.currentTimeMillis()
        val status = if (dataSelecionada < hoje) "PASSADA" else "FUTURA"

        if (status == "PASSADA" && (golsA == null || golsB == null)) {
            Toast.makeText(this, "Para jogos passados, preencha o resultado real", Toast.LENGTH_SHORT).show()
            return
        }

        val partida = Partida(
            id = if (isEdicao) partidaId else 0,
            usuarioId = usuarioId,
            timeA = timeA,
            timeB = timeB,
            golsA = golsA,
            golsB = golsB,
            previsaoGolsA = previsaoA,
            previsaoGolsB = previsaoB,
            imagemUriTimeA = imagemPathTimeA,
            imagemUriTimeB = imagemPathTimeB,
            observacoes = if (observacoes.isNotEmpty()) observacoes else null,
            data = dataSelecionada,
            status = status
        )

        lifecycleScope.launch {
            if (isEdicao) {
                repository.atualizar(partida)
                Toast.makeText(this@AdicionarPartidaActivity, "Partida atualizada!", Toast.LENGTH_SHORT).show()
            } else {
                repository.adicionar(partida)
                Toast.makeText(this@AdicionarPartidaActivity, "Partida adicionada!", Toast.LENGTH_SHORT).show()
            }
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupValidacaoTempoReal() {
        binding.etTimeA.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.tilTimeA.error = if (s.isNullOrBlank()) "Nome do time é obrigatório" else null
            }
        })

        binding.etTimeB.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.tilTimeB.error = if (s.isNullOrBlank()) "Nome do time é obrigatório" else null
            }
        })

        binding.etPrevisaoA.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.tilPrevisaoA.error = if (s.isNullOrBlank()) "Previsão obrigatória" else null
            }
        })

        binding.etPrevisaoB.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.tilPrevisaoB.error = if (s.isNullOrBlank()) "Previsão obrigatória" else null
            }
        })

        binding.etGolsA.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val texto = s.toString().trim()
                binding.tilGolsA.error = if (texto.isNotEmpty() && texto.toIntOrNull() == null) "Apenas números" else null
            }
        })

        binding.etGolsB.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val texto = s.toString().trim()
                binding.tilGolsB.error = if (texto.isNotEmpty() && texto.toIntOrNull() == null) "Apenas números" else null
            }
        })
    }

    private fun carregarPartida() {
        lifecycleScope.launch {
            repository.buscarTodas(usuarioId).collect { lista ->
                val partida = lista.find { it.id == partidaId }
                if (partida != null) {
                    binding.etTimeA.setText(partida.timeA)
                    binding.etTimeB.setText(partida.timeB)

                    dataSelecionada = partida.data
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.etData.setText(sdf.format(java.util.Date(partida.data)))

                    partida.golsA?.let { binding.etGolsA.setText(it.toString()) }
                    partida.golsB?.let { binding.etGolsB.setText(it.toString()) }
                    partida.previsaoGolsA?.let { binding.etPrevisaoA.setText(it.toString()) }
                    partida.previsaoGolsB?.let { binding.etPrevisaoB.setText(it.toString()) }

                    partida.imagemUriTimeA?.let { path ->
                        try {
                            val file = File(path)
                            if (file.exists()) {
                                binding.ivImagemTimeA.setImageURI(Uri.fromFile(file))
                                binding.ivImagemTimeA.visibility = View.VISIBLE
                                imagemPathTimeA = path
                            }
                        } catch (e: Exception) {
                            binding.ivImagemTimeA.visibility = View.GONE
                        }
                    }

                    partida.imagemUriTimeB?.let { path ->
                        try {
                            val file = File(path)
                            if (file.exists()) {
                                binding.ivImagemTimeB.setImageURI(Uri.fromFile(file))
                                binding.ivImagemTimeB.visibility = View.VISIBLE
                                imagemPathTimeB = path
                            }
                        } catch (e: Exception) {
                            binding.ivImagemTimeB.visibility = View.GONE
                        }
                    }

                    partida.observacoes?.let { binding.etObservacoes.setText(it) }

                    val hoje = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }

                    if (Calendar.getInstance().apply { timeInMillis = partida.data }.before(hoje)) {
                        binding.tvResultadoInfo.text = "Jogo já aconteceu - preencha o resultado"
                        binding.layoutResultadoReal.alpha = 1f
                        binding.etGolsA.isEnabled = true
                        binding.etGolsB.isEnabled = true
                    } else {
                        binding.tvResultadoInfo.text = "Jogo ainda não aconteceu - apenas previsão"
                        binding.layoutResultadoReal.alpha = 0.5f
                        binding.etGolsA.isEnabled = false
                        binding.etGolsB.isEnabled = false
                    }
                }
            }
        }
    }
}