package com.example.bolaodapelada.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.bolaodapelada.data.entity.Partida
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun gerarRelatorio(context: Context, partidas: List<Partida>, usuarioNome: String): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val paintTitulo = Paint().apply {
            color = Color.parseColor("#6A0D83")
            textSize = 24f
            isFakeBoldText = true
        }

        val paintSubtitulo = Paint().apply {
            color = Color.parseColor("#757575")
            textSize = 14f
        }

        val paintTexto = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        val paintVerde = Paint().apply {
            color = Color.parseColor("#4CAF50")
            textSize = 12f
            isFakeBoldText = true
        }

        val paintVermelho = Paint().apply {
            color = Color.parseColor("#EE5D6C")
            textSize = 12f
            isFakeBoldText = true
        }

        val paintHeader = Paint().apply {
            color = Color.parseColor("#6A0D83")
            textSize = 14f
            isFakeBoldText = true
        }

        var y = 50f

        canvas.drawText("Bolão da Pelada - Relatório", 40f, y, paintTitulo)
        y += 30f
        canvas.drawText("Usuário: $usuarioNome", 40f, y, paintSubtitulo)
        y += 20f

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Gerado em: ${sdf.format(Date())}", 40f, y, paintSubtitulo)
        y += 40f

        val partidasPassadas = partidas.filter { it.status == "PASSADA" }
        val totalJogos = partidasPassadas.size
        val acertos = partidasPassadas.count { it.golsA == it.previsaoGolsA && it.golsB == it.previsaoGolsB }
        val taxaAcerto = if (totalJogos > 0) (acertos * 100) / totalJogos else 0

        canvas.drawText("RESUMO GERAL", 40f, y, paintHeader)
        y += 25f
        canvas.drawText("Total de Jogos: $totalJogos", 40f, y, paintTexto)
        y += 20f
        canvas.drawText("Acertos: $acertos", 40f, y, paintVerde)
        y += 20f
        canvas.drawText("Erros: ${totalJogos - acertos}", 40f, y, paintVermelho)
        y += 20f
        canvas.drawText("Taxa de Acerto: $taxaAcerto%", 40f, y, paintTexto)
        y += 40f

        canvas.drawText("HISTÓRICO DE PARTIDAS", 40f, y, paintHeader)
        y += 25f

        val sdfData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (partida in partidas.sortedByDescending { it.data }) {
            if (y > 780f) {
                pdfDocument.finishPage(page)
                val newPage = pdfDocument.startPage(pageInfo)
                canvas = newPage.canvas
                y = 50f
            }

            val dataStr = sdfData.format(Date(partida.data))
            val resultado = if (partida.status == "PASSADA") {
                "${partida.golsA} x ${partida.golsB}"
            } else {
                "A definir"
            }
            val previsao = "${partida.previsaoGolsA} x ${partida.previsaoGolsB}"

            val acertou = partida.status == "PASSADA" &&
                    partida.golsA == partida.previsaoGolsA &&
                    partida.golsB == partida.previsaoGolsB

            val statusTexto = when {
                partida.status == "FUTURA" -> "[FUTURO]"
                acertou -> "[ACERTOU ✓]"
                else -> "[ERROU ✗]"
            }

            val paintStatus = when {
                partida.status == "FUTURA" -> paintTexto
                acertou -> paintVerde
                else -> paintVermelho
            }

            canvas.drawText("$dataStr | ${partida.timeA} x ${partida.timeB}", 40f, y, paintTexto)
            y += 18f
            canvas.drawText("   Resultado: $resultado | Previsão: $previsao | $statusTexto", 40f, y, paintStatus)
            y += 25f
        }

        pdfDocument.finishPage(page)

        try {
            val file = File(context.cacheDir, "bolao_relatorio_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            return file
        } catch (e: Exception) {
            pdfDocument.close()
            return null
        }
    }
}