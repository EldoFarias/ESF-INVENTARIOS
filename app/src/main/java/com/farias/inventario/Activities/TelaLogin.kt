package com.farias.inventario.Activities

import GerenciadorDeFaturamento
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.farias.inventario.BancoDados.UsuarioDAO
import com.farias.inventario.Utilidades.DadosUsuarioLogado
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaLoginBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaLogin : AppCompatActivity() {

    private lateinit var gerenciadorDeFaturamento: GerenciadorDeFaturamento
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val binding by lazy {
        ActivityTelaLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        gerenciadorDeFaturamento = GerenciadorDeFaturamento.getInstance(this)

        inicializarToolbar()
        inicializarEventosClick()

        coroutineScope.launch {
            if (temInternet()) {
                delay(3000)  // Aguardar 2 segundos
                verificarLicencaAtiva()
            } else {
                withContext(Dispatchers.Main) {
                    exibirMensagem("Verifique sua conexão de internet.")
                    binding.progressBarLogin.visibility = View.GONE  // Esconder ProgressBar
                    binding.txtMsg.visibility = View.GONE // ESCONDER MSG
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onRestart() {
        super.onRestart()
        binding.progressBarLogin.visibility = View.VISIBLE  // Mostrar ProgressBar
        binding.txtMsg.visibility = View.VISIBLE // Mostrar MSG
        coroutineScope.launch {
            if (temInternet()) {
                delay(3000)  // Aguardar 2 segundos
                verificarLicencaAtiva()
            } else {
                withContext(Dispatchers.Main) {
                    exibirMensagem("Verifique sua conexão de internet.")
                    binding.progressBarLogin.visibility = View.GONE  // Esconder ProgressBar
                    binding.txtMsg.visibility = View.GONE // ESCONDER MSG
                    binding.btnEntrarSistema.visibility = View.GONE
                }
            }
        }
    }


    private fun temInternet(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

  /*  private fun verificarLicencaAtiva() {
        Log.i("TelaLoginResumido", "verificarLicencaAtiva: Chamado")
        gerenciadorDeFaturamento.verificarAssinaturasAtivas { assinaturaAtiva ->
            coroutineScope.launch(Dispatchers.Main) {
                if (assinaturaAtiva) {
                    Log.i("TelaLoginResumido", "verificarLicencaAtiva: Assinatura ativa")
                    binding.btnEntrarSistema.isEnabled = true
                } else {
                    Log.i("TelaLoginResumido", "verificarLicencaAtiva: Nenhuma assinatura ativa")
                    binding.btnEntrarSistema.isEnabled = false
                }
            }
        }
    }

   */

    private fun verificarLicencaAtiva() {
        Log.i("TelaLoginResumido", "verificarLicencaAtiva: Chamado")
        val resultado = CompletableDeferred<Boolean>()
        gerenciadorDeFaturamento.verificarAssinaturasAtivas { assinaturaAtiva ->
            resultado.complete(assinaturaAtiva)
        }

        coroutineScope.launch(Dispatchers.Main) {
            val hasActiveSubscription = resultado.await()
            binding.progressBarLogin.visibility = View.GONE  // Esconder ProgressBar
            binding.txtMsg.visibility = View.GONE // ESCONDER MSG
            if (hasActiveSubscription) {
                Log.i("TelaLoginResumido", "verificarLicencaAtiva: Assinatura ativa")
                binding.btnEntrarSistema.visibility = View.VISIBLE
            } else {
                Log.i("TelaLoginResumido", "verificarLicencaAtiva: Nenhuma assinatura ativa")
                binding.btnEntrarSistema.visibility = View.INVISIBLE
                binding.txtMsg.text = "Adquira uma assinatura."
            }
        }
    }

    private fun inicializarEventosClick() {

        binding.btnEntrarSistema.setOnClickListener {

            val codigo = binding.edtCodigoAcesso.text.toString()
            val senha = binding.edtSenha.text.toString()

            if (codigo.isNotEmpty() && senha.isNotEmpty()) {
                val usuarioDAO = UsuarioDAO(this)

                if (usuarioDAO.fazerLogin(codigo, senha)) {

                    val dadosUsuarioLogado = DadosUsuarioLogado.getInstance()
                    dadosUsuarioLogado.codigoUsuario = codigo
                    dadosUsuarioLogado.statusUsuario = "logado"

                    val intent = Intent(this, TelaPrincipal::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    exibirMensagem("Codigo de acesso ou senha inválidos")
                }
            } else {
                exibirMensagem("Preencha os campo de código de acesso e ou senha")
            }

        }

        binding.txtCriarConta.setOnClickListener {
            val intent = Intent(this, TelaCadastroUsuario::class.java)
            startActivity(intent)
        }

        binding.edtCodigoAcesso.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.edtCodigoAcesso.text.toString().isNotEmpty()) {
                    val usuarioDAO = UsuarioDAO(this)
                    val nomeUsuario =
                        usuarioDAO.verificarCodigoUsuario(binding.edtCodigoAcesso.text.toString())
                    binding.txtNomeUsuario.visibility = View.VISIBLE
                    binding.txtNomeUsuario.text = nomeUsuario
                } else {
                    binding.txtNomeUsuario.visibility = View.INVISIBLE
                    binding.txtNomeUsuario.text = ""
                }
            }
        }

        binding.btnConsultarAssinaturas.setOnClickListener {
            val intent = Intent(this, TelaAssinaturas::class.java)
            startActivity(intent)
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeLogin.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Login usuário local"
            subtitle = "Usuário inventário local"
            // setDisplayHomeAsUpEnabled(true)
        }
    }
}