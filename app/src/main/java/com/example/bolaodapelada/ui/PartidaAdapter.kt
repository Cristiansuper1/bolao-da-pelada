package com.example.bolaodapelada.ui

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bolaodapelada.data.entity.Partida
import com.example.bolaodapelada.databinding.ItemPartidaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PartidaAdapter(
    private var partidas: List<Partida>,
    private val onEditarClick: (Partida) -> Unit,
    private val onExcluirClick: (Partida) -> Unit
) : RecyclerView.Adapter<PartidaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPartidaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPartidaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partida = partidas[position]
        val binding = holder.binding

        binding.tvTimeA.text = partida.timeA
        binding.tvTimeB.text = partida.timeB

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.tvData.text = sdf.format(Date(partida.data))

        binding.tvPrevisao.text = "${partida.previsaoGolsA} x ${partida.previsaoGolsB}"

        try {
            partida.imagemUriTimeA?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) {
                    binding.ivLogoTimeA.setImageURI(Uri.fromFile(file))
                    binding.ivLogoTimeA.visibility = View.VISIBLE
                } else {
                    binding.ivLogoTimeA.visibility = View.GONE
                }
            } ?: run {
                binding.ivLogoTimeA.visibility = View.GONE
            }
        } catch (e: Exception) {
            binding.ivLogoTimeA.visibility = View.GONE
        }

        try {
            partida.imagemUriTimeB?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) {
                    binding.ivLogoTimeB.setImageURI(Uri.fromFile(file))
                    binding.ivLogoTimeB.visibility = View.VISIBLE
                } else {
                    binding.ivLogoTimeB.visibility = View.GONE
                }
            } ?: run {
                binding.ivLogoTimeB.visibility = View.GONE
            }
        } catch (e: Exception) {
            binding.ivLogoTimeB.visibility = View.GONE
        }

        if (partida.status == "PASSADA") {
            binding.tvPlacarReal.text = "${partida.golsA} x ${partida.golsB}"

            val acertou = partida.golsA == partida.previsaoGolsA && partida.golsB == partida.previsaoGolsB

            if (acertou) {
                binding.cardPartida.strokeColor = Color.parseColor("#4CAF50")
            } else {
                binding.cardPartida.strokeColor = Color.parseColor("#EE5D6C")
            }
        } else {
            binding.tvPlacarReal.text = "A definir"
            binding.cardPartida.strokeColor = Color.parseColor("#6A0D83")
        }

        binding.ivEditar.setOnClickListener { onEditarClick(partida) }
        binding.ivExcluir.setOnClickListener { onExcluirClick(partida) }
    }

    override fun getItemCount() = partidas.size

    fun atualizarLista(novaLista: List<Partida>) {
        partidas = novaLista
        notifyDataSetChanged()
    }
}