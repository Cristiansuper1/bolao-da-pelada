package com.example.bolaodapelada.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bolaodapelada.data.AppDatabase
import com.example.bolaodapelada.data.entity.Partida
import com.example.bolaodapelada.data.repository.PartidaRepository
import com.example.bolaodapelada.databinding.FragmentListaPartidasBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ListaPartidasFragment : Fragment() {

    private var _binding: FragmentListaPartidasBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: PartidaRepository
    private var usuarioId: Long = -1
    private var status: String = "FUTURA"
    private lateinit var adapter: PartidaAdapter
    private var todasAsPartidas: List<Partida> = emptyList()
    private var termoBusca: String = ""
    private var modoOrdenacao: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            usuarioId = it.getLong("USUARIO_ID", -1)
            status = it.getString("STATUS", "FUTURA")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListaPartidasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        repository = PartidaRepository(database.partidaDao())

        adapter = PartidaAdapter(emptyList(), ::editarPartida, ::confirmarExclusao)
        binding.recyclerViewPartidas.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPartidas.adapter = adapter

        observarDados()
    }

    private fun observarDados() {
        viewLifecycleOwner.lifecycleScope.launch {
            val flow = if (status == "FUTURA") repository.buscarFuturas(usuarioId) else repository.buscarPassadas(usuarioId)
            flow.collect { lista ->
                todasAsPartidas = lista
                aplicarFiltroEOrdem()
            }
        }
    }

    fun aplicarFiltro(termo: String = termoBusca) {
        termoBusca = termo
        aplicarFiltroEOrdem()
    }

    fun aplicarOrdenacao(modo: Int) {
        modoOrdenacao = modo
        aplicarFiltroEOrdem()
    }

    private fun aplicarFiltroEOrdem() {
        var lista = if (termoBusca.isBlank()) {
            todasAsPartidas
        } else {
            todasAsPartidas.filter { partida ->
                partida.timeA.contains(termoBusca, ignoreCase = true) ||
                        partida.timeB.contains(termoBusca, ignoreCase = true)
            }
        }

        lista = when (modoOrdenacao) {
            1 -> lista.sortedByDescending { it.data }
            2 -> lista.sortedBy { it.timeA.lowercase() }
            3 -> if (status == "PASSADA") {
                lista.sortedByDescending { partida ->
                    val acertou = partida.golsA == partida.previsaoGolsA && partida.golsB == partida.previsaoGolsB
                    if (acertou) 1 else 0
                }
            } else {
                lista.sortedBy { it.data }
            }
            else -> lista.sortedBy { it.data }
        }

        adapter.atualizarLista(lista)
        binding.tvVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun editarPartida(partida: Partida) {
        val intent = Intent(context, AdicionarPartidaActivity::class.java)
        intent.putExtra("USUARIO_ID", usuarioId)
        intent.putExtra("PARTIDA_ID", partida.id)
        startActivity(intent)
    }

    private fun confirmarExclusao(partida: Partida) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Partida")
            .setMessage("Tem certeza que deseja excluir ${partida.timeA} x ${partida.timeB}?")
            .setPositiveButton("Excluir") { _, _ ->
                excluirComDesfazer(partida)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirComDesfazer(partida: Partida) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.excluir(partida)

            Snackbar.make(binding.root, "Partida excluída", Snackbar.LENGTH_LONG)
                .setAction("Desfazer") {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val partidaRestaurada = partida.copy(id = 0)
                        repository.adicionar(partidaRestaurada)
                        Snackbar.make(binding.root, "Partida restaurada!", Snackbar.LENGTH_SHORT).show()
                    }
                }
                .setActionTextColor(resources.getColor(com.example.bolaodapelada.R.color.accent, null))
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(usuarioId: Long, status: String): ListaPartidasFragment {
            val fragment = ListaPartidasFragment()
            val args = Bundle()
            args.putLong("USUARIO_ID", usuarioId)
            args.putString("STATUS", status)
            fragment.arguments = args
            return fragment
        }
    }
}