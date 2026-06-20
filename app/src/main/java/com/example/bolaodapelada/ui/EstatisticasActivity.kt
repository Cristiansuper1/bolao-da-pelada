package com.example.bolaodapelada.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bolaodapelada.data.AppDatabase
import com.example.bolaodapelada.data.entity.Partida
import com.example.bolaodapelada.data.repository.PartidaRepository
import com.example.bolaodapelada.databinding.ActivityEstatisticasBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EstatisticasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstatisticasBinding
    private lateinit var repository: PartidaRepository
    private var usuarioId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstatisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        repository = PartidaRepository(database.partidaDao())

        usuarioId = intent.getLongExtra("USUARIO_ID", -1)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        carregarEstatisticas()
    }

    private fun configurarGraficoMensal(partidasPassadas: List<Partida>) {
        val sdfMes = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val dadosPorMes = mutableMapOf<String, Pair<Int, Int>>()

        partidasPassadas.sortedBy { it.data }.forEach { partida ->
            val chaveMes = sdfMes.format(Date(partida.data))
            val (acertosAtuais, totalAtual) = dadosPorMes.getOrDefault(chaveMes, Pair(0, 0))
            val acertou = partida.golsA == partida.previsaoGolsA && partida.golsB == partida.previsaoGolsB
            dadosPorMes[chaveMes] = Pair(
                acertosAtuais + if (acertou) 1 else 0,
                totalAtual + 1
            )
        }

        val entries = dadosPorMes.toList().mapIndexed { index, (_, par) ->
            val taxa = if (par.second > 0) (par.first * 100f) / par.second else 0f
            Entry(index.toFloat(), taxa)
        }

        if (entries.isEmpty()) {
            binding.lineChartMensal.setNoDataText("Sem dados suficientes")
            return
        }

        val dataSet = LineDataSet(entries, "Taxa de Acerto (%)")
        dataSet.color = Color.parseColor("#CF4A93")
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(Color.parseColor("#CF4A93"))
        dataSet.valueTextSize = 10f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#CF4A93")
        dataSet.fillAlpha = 50
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val data = LineData(dataSet)
        binding.lineChartMensal.data = data
        binding.lineChartMensal.description.isEnabled = false

        val labels = dadosPorMes.keys.toList()
        binding.lineChartMensal.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.lineChartMensal.xAxis.setDrawGridLines(false)
        binding.lineChartMensal.xAxis.granularity = 1f
        binding.lineChartMensal.axisLeft.axisMinimum = 0f
        binding.lineChartMensal.axisLeft.axisMaximum = 100f
        binding.lineChartMensal.legend.isEnabled = false
        binding.lineChartMensal.invalidate()
    }

    private fun carregarEstatisticas() {
        lifecycleScope.launch {
            repository.buscarTodas(usuarioId).collect { partidas ->
                val partidasPassadas = partidas.filter { it.status == "PASSADA" }

                val totalJogos = partidasPassadas.size
                var acertos = 0
                var erros = 0

                val contagemTimes = mutableMapOf<String, Int>()
                val acertosPorTime = mutableMapOf<String, Int>()
                val totalPorTime = mutableMapOf<String, Int>()

                partidasPassadas.forEach { partida ->
                    val acertou = partida.golsA == partida.previsaoGolsA && partida.golsB == partida.previsaoGolsB

                    if (acertou) {
                        acertos++
                    } else {
                        erros++
                    }

                    contagemTimes[partida.timeA] = contagemTimes.getOrDefault(partida.timeA, 0) + 1
                    contagemTimes[partida.timeB] = contagemTimes.getOrDefault(partida.timeB, 0) + 1

                    totalPorTime[partida.timeA] = totalPorTime.getOrDefault(partida.timeA, 0) + 1
                    totalPorTime[partida.timeB] = totalPorTime.getOrDefault(partida.timeB, 0) + 1

                    if (acertou) {
                        acertosPorTime[partida.timeA] = acertosPorTime.getOrDefault(partida.timeA, 0) + 1
                        acertosPorTime[partida.timeB] = acertosPorTime.getOrDefault(partida.timeB, 0) + 1
                    }
                }

                val taxaAcerto = if (totalJogos > 0) (acertos * 100) / totalJogos else 0

                binding.tvTaxaAcerto.text = "$taxaAcerto%"
                binding.tvTotalJogos.text = totalJogos.toString()
                binding.tvAcertos.text = acertos.toString()
                binding.tvErros.text = erros.toString()

                val timeMaisPrevisto = contagemTimes.maxByOrNull { it.value }?.key ?: "-"
                binding.tvTimeMaisPrevisto.text = timeMaisPrevisto

                var timeMaisAcertado = "-"
                var maiorTaxaAcerto = 0.0
                totalPorTime.forEach { (time, total) ->
                    if (total >= 2) {
                        val acertosTime = acertosPorTime.getOrDefault(time, 0)
                        val taxa = (acertosTime * 100.0) / total
                        if (taxa > maiorTaxaAcerto) {
                            maiorTaxaAcerto = taxa
                            timeMaisAcertado = time
                        }
                    }
                }
                binding.tvTimeMaisAcertado.text = if (timeMaisAcertado == "-") "-" else "$timeMaisAcertado (${maiorTaxaAcerto.toInt()}%)"

                configurarGraficoRosca(acertos, erros)
                configurarGraficoBarras(contagemTimes)
                configurarGraficoMensal(partidasPassadas)
            }
        }
    }

    private fun configurarGraficoRosca(acertos: Int, erros: Int) {
        val entries = mutableListOf<PieEntry>()
        if (acertos > 0) entries.add(PieEntry(acertos.toFloat(), "Acertos"))
        if (erros > 0) entries.add(PieEntry(erros.toFloat(), "Erros"))

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "Sem dados"))
        }

        val dataSet = PieDataSet(entries, "Resultados")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),
            Color.parseColor("#EE5D6C"),
            Color.parseColor("#757575")
        )
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        binding.pieChartAcerto.data = data
        binding.pieChartAcerto.description.isEnabled = false
        binding.pieChartAcerto.setCenterText("Taxa de Acerto")
        binding.pieChartAcerto.setCenterTextSize(16f)
        binding.pieChartAcerto.setUsePercentValues(true)
        binding.pieChartAcerto.invalidate()
    }

    private fun configurarGraficoBarras(contagemTimes: Map<String, Int>) {
        val sortedTimes = contagemTimes.toList().sortedByDescending { it.second }.take(10).toMutableList()

        val entries = sortedTimes.mapIndexed { index, (time, count) ->
            BarEntry(index.toFloat(), count.toFloat())
        }.toMutableList()

        if (entries.isEmpty()) {
            entries.add(BarEntry(0f, 0f))
        }

        val dataSet = BarDataSet(entries, "Previsões")
        dataSet.color = Color.parseColor("#6A0D83")
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)
        binding.barChartTimes.data = data
        binding.barChartTimes.description.isEnabled = false

        val labels = sortedTimes.map { it.first }.toMutableList()
        if (labels.isEmpty()) labels.add("Sem dados")

        binding.barChartTimes.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChartTimes.xAxis.setDrawGridLines(false)
        binding.barChartTimes.xAxis.granularity = 1f
        binding.barChartTimes.legend.isEnabled = false
        binding.barChartTimes.invalidate()
    }
}