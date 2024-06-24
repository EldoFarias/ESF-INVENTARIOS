package com.farias.inventario.Activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.farias.inventario.R
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaDesenvolvedorBinding

class TelaDesenvolvedor : AppCompatActivity() {

    private val binding by lazy { ActivityTelaDesenvolvedorBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarEventosClick()

    }

    private fun inicializarEventosClick() {
        binding.contatoWhatsApp.setOnClickListener {
            var temWhats = false
            val msg = binding.msgWhats.text.toString()
            val numero = "5511959069133" // Inclua o código do país

            // Cria a URL com a mensagem codificada
            val url = "https://wa.me/${numero}?text=${Uri.encode(msg)}"
            Log.d("WhatsApp", "URL gerada: $url")

            // Cria a Intent para abrir o link do WhatsApp
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }

            try {
                startActivity(intent)
                temWhats = true
                Log.d("WhatsApp", "Intent iniciada com sucesso")
            } catch (e: ActivityNotFoundException) {
                exibirMensagem("WhatsApp não está instalado.")
                Log.e("WhatsApp", "Erro ao iniciar a Intent: WhatsApp não está instalado.", e)
                temWhats = false
            } finally {
                if (!temWhats) {
                    // Intent para abrir a página do WhatsApp na Play Store
                    val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"))

                    // Verifica se há algum aplicativo capaz de lidar com a Intent da Play Store
                    if (playStoreIntent.resolveActivity(packageManager) != null) {
                        startActivity(playStoreIntent)
                    } else {
                        exibirMensagem("Google Play Store não está disponível.")
                        Log.e("WhatsApp", "Google Play Store não está disponível.")
                    }
                }
            }
        }
    }
}