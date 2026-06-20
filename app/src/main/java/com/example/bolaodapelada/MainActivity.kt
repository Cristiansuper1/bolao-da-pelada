package com.example.bolaodapelada

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bolaodapelada.databinding.ActivityMainBinding
import com.example.bolaodapelada.ui.AdicionarPartidaActivity
import com.example.bolaodapelada.ui.ConfiguracoesActivity
import com.example.bolaodapelada.ui.EstatisticasActivity
import com.example.bolaodapelada.ui.ListaPartidasFragment
import com.example.bolaodapelada.ui.PartidaPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var usuarioId: Long = -1
    private lateinit var pagerAdapter: PartidaPagerAdapter
    private var modoOrdenacaoAtual: Int = 0

    private val adicionarPartidaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Operação realizada com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioId = intent.getLongExtra("USUARIO_ID", -1)

        pagerAdapter = PartidaPagerAdapter(this, usuarioId)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Futuros" else "Passados"
        }.attach()

        binding.etBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val termo = s.toString().trim()
                val fragmentAtual = supportFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}") as? ListaPartidasFragment
                fragmentAtual?.aplicarFiltro(termo)
            }
        })

        binding.ivOrdenar.setOnClickListener {
            mostrarDialogoOrdenacao()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_estatisticas -> {
                    val intent = Intent(this, EstatisticasActivity::class.java)
                    intent.putExtra("USUARIO_ID", usuarioId)
                    startActivity(intent)
                    true
                }
                R.id.action_configuracoes -> {
                    val intent = Intent(this, ConfiguracoesActivity::class.java)
                    intent.putExtra("USUARIO_ID", usuarioId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        binding.fabAdicionar.setOnClickListener {
            val intent = Intent(this, AdicionarPartidaActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioId)
            adicionarPartidaLauncher.launch(intent)
        }
    }

    private fun mostrarDialogoOrdenacao() {
        val opcoes = arrayOf(
            "Data do Jogo (mais próxima)",
            "Data do Jogo (mais distante)",
            "Nome do Time (A-Z)",
            "Taxa de Acerto"
        )

        AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setSingleChoiceItems(opcoes, modoOrdenacaoAtual) { dialog, which ->
                modoOrdenacaoAtual = which
                val fragmentAtual = supportFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}") as? ListaPartidasFragment
                fragmentAtual?.aplicarOrdenacao(which)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}