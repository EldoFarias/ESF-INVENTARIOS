package com.farias.inventario.Activities

import DivergenciaDAO
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import com.farias.inventario.BancoDados.ConfiguracaoDAO
import com.farias.inventario.BancoDados.ContagemDAO
import com.farias.inventario.BancoDados.DROP_TABLES_DAO
import com.farias.inventario.BancoDados.DatabaseHelper
import com.farias.inventario.BancoDados.EnderecoDAO
import com.farias.inventario.BancoDados.TransmitirReceberContagensDAO
import com.farias.inventario.Modelos.Itens
import com.farias.inventario.R
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaPrincipalBinding

class TelaPrincipal : AppCompatActivity() {

    private lateinit var createDocumentLauncher: ActivityResultLauncher<Intent>
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>

    private var contexto: Context? = null
    private var config: Boolean = false

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
        private const val CREATE_FILE_REQUEST_CODE = 1

    }

    private val binding by lazy {
        ActivityTelaPrincipalBinding.inflate(layoutInflater)
    }

    private var listaItens = emptyList<Itens>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        createDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    data?.data?.let { uri ->
                        val databaseHelper = DatabaseHelper(this)
                        val sucesso = databaseHelper.gerarBackup(this, uri)

                        if (sucesso) {
                            exibirMensagem("Backup salvo com sucesso!")
                        } else {
                            exibirMensagem("Erro ao gerar backup")
                        }
                    }
                }
            }

        openDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    data?.data?.let { uri ->
                        val databaseHelper = DatabaseHelper(this)
                        val sucesso = databaseHelper.restaurarBackup(this, uri)

                        if (sucesso) {
                            exibirMensagem("Backup restaurado com sucesso")
                        } else {
                            exibirMensagem("Erro ao restaurar backup")
                        }
                    }
                }
            }

        verificarESolicitarPermissoes()

        contexto = this
        configuracoesSistema()
        criarMenu()

        inicializarToolbar()
        inicializarEventosClick()
        verificarCabecalhoTeste()

        alimentarDashBoardsEnderecos()

    }

    private fun alimentarDashBoardsEnderecos() {
        val enderecosDao = EnderecoDAO.getInstance(this)
        val contagemDao = ContagemDAO.getInstance(this)

        val quantidadeEndereco = enderecosDao.totalEnderecos()
        val quantidadeEnderecoAbertos = enderecosDao.totalAbertos()
        val quantidadeEnderecoFechado = enderecosDao.totalFechados()

        val quantidadeItens1COntagem = contagemDao.somarItensContagem1()
        val quantidadeItens2COntagem = contagemDao.somarItensContagem2()
        val quantidadeItens3COntagem = contagemDao.somarItensContagem3()

        val quantidadePc1COntagem = contagemDao.somarPecasContagem1()
        val quantidadePc2COntagem = contagemDao.somarPecasContagem2()
        val quantidadePc3COntagem = contagemDao.somarPecasContagem3()

        binding.txtQtdEnderecoTotal.text = quantidadeEndereco.toString()
        binding.txtQdtEnderecoAberto.text = quantidadeEnderecoAbertos.toString()
        binding.txtQdtEnderecoFechados.text = quantidadeEnderecoFechado.toString()

        binding.txtTotalPcsPrimeiraCont.text = quantidadeItens1COntagem.toString()
        binding.txtTotalPcsSegundaCont.text = quantidadeItens2COntagem.toString()
        binding.txtTotalPcsTerceiraCont.text = quantidadeItens3COntagem.toString()

        binding.txtQtdContado1.text = quantidadePc1COntagem.toString()
        binding.txtQtdContado2.text = quantidadePc2COntagem.toString()
        binding.txtQtdContado3.text = quantidadePc3COntagem.toString()

    }

    private fun exibirDialogSenha(senha: String, targetActivity: Class<*>) {
        val dialogView = layoutInflater.inflate(R.layout.item_alert_dialog, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(false)

        dialog.setPositiveButton("OK") { dialog, which ->
            val senhaDigitada = dialogView.findViewById<EditText>(R.id.editTextSenha)
            val senhaFormatada = senhaDigitada.text.trim().toString()

            if (senhaFormatada == senha) {
                val intent = Intent(applicationContext, targetActivity)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                exibirAlertaSenhaIncorreta()
            }
        }

        dialog.setNegativeButton("Cancelar") { dialog, which ->
            dialog.cancel()
        }

        dialog.show()
    }

    private fun exibirDialogrestaurarBkp(senha: String, onSuccess: () -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.item_alert_dialog, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(false)

        dialog.setPositiveButton("OK") { dialog, which ->
            val senhaDigitada = dialogView.findViewById<EditText>(R.id.editTextSenha)
            val senhaFormatada = senhaDigitada.text.trim().toString()

            if (senhaFormatada == senha) {
                onSuccess()
            } else {
                exibirAlertaSenhaIncorreta()
            }
        }

        dialog.setNegativeButton("Cancelar") { dialog, which ->
            dialog.cancel()
        }

        dialog.show()
    }


    private fun exibirAlertaSenhaIncorreta() {
        val dialogView = layoutInflater.inflate(R.layout.item_alert_dialog_senha_errada, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(false)

        dialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun criarMenu() {
        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_principal, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {

                        R.id.menu_configuracoes -> {

                            val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                            val configuracoes = configuracaoDao.verificarConfiguracao()
                            val senha = configuracoes?.senhaAcesso.toString()

                            if (checarConfiguracoes()) {
                                if (senha == "99999") {
                                    val intent =
                                        Intent(applicationContext, TelaConfiguracoes::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                } else {
                                    exibirDialogSenha(senha, TelaConfiguracoes::class.java)
                                }
                            } else {
                                val intent =
                                    Intent(applicationContext, TelaConfiguracoes::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }

                        }

                        R.id.menu_gerar_backup -> {
                            criarBackUp()
                            /*  val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                              val configuracoes = configuracaoDao.verificarConfiguracao()
                              val senha = configuracoes?.senhaAcesso.toString()

                              if (senha == "99999") {
                                  criarBackUp()
                              }else{
                                  exibirDialogrestaurarBkp(senha, { criarBackUp() })
                              }

                             */

                        }

                        R.id.menu_restaurar_backup -> {

                            val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                            val configuracoes = configuracaoDao.verificarConfiguracao()
                            val senha = configuracoes?.senhaAcesso.toString()

                            if (checarConfiguracoes()) {
                                if (senha == "99999") {
                                    selecionarBackupParaRestaurar()
                                } else {
                                    exibirDialogrestaurarBkp(
                                        senha,
                                        { selecionarBackupParaRestaurar() })
                                }
                            } else {
                                selecionarBackupParaRestaurar()
                            }

                        }

                        R.id.menu_sair -> {
                            val intent = Intent(applicationContext, TelaLogin::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)

                        }

                        R.id.menu_cadastro_produto -> {

                            val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                            val configuracoes = configuracaoDao.verificarConfiguracao()
                            val senha = configuracoes?.senhaAcesso.toString()

                            if (checarConfiguracoes()) {
                                if (senha == "99999") {
                                    if (config) {
                                        val intent =
                                            Intent(applicationContext, TelaProdutos::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(intent)
                                    } else {
                                        exibirMensagem("Altere as configurações")
                                    }
                                } else {
                                    exibirDialogSenha(senha, TelaProdutos::class.java)
                                }
                            } else {
                                if (config) {
                                    val intent =
                                        Intent(applicationContext, TelaProdutos::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                } else {
                                    exibirMensagem("Altere as configurações")
                                }
                            }


                        }

                        R.id.menu_cadastrar_endereco -> {
                            val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                            val configuracoes = configuracaoDao.verificarConfiguracao()
                            val senha = configuracoes?.senhaAcesso.toString()

                            if (checarConfiguracoes()) {
                                if (senha == "99999") {
                                    if (config) {
                                        val intent =
                                            Intent(
                                                applicationContext,
                                                TelaCadastroEndereco::class.java
                                            )
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(intent)
                                    } else {
                                        exibirMensagem("Altere as configurações")
                                    }
                                } else {
                                    exibirDialogSenha(senha, TelaCadastroEndereco::class.java)
                                }
                            } else {
                                if (config) {
                                    val intent =
                                        Intent(applicationContext, TelaCadastroEndereco::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                } else {
                                    exibirMensagem("Altere as configurações")
                                }
                            }


                        }

                        R.id.menu_analisar_contagens -> {

                            val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                            val configuracoes = configuracaoDao.verificarConfiguracao()
                            val senha = configuracoes?.senhaAcesso.toString()

                            if (checarConfiguracoes()) {
                                if (senha == "99999") {
                                    analisarContagens()
                                } else {
                                    exibirDialogrestaurarBkp(senha, { analisarContagens() })
                                }
                            } else {
                                analisarContagens()
                            }

                        }

                        R.id.menu_sair -> {
                            val intent = Intent(applicationContext, TelaLogin::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)

                        }

                        R.id.menu_relatorio_contagens -> {

                            val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                            val configuracoes = configuracaoDao.verificarConfiguracao()
                            val senha = configuracoes?.senhaAcesso.toString()

                            if (checarConfiguracoes()) {
                                if (senha == "99999") {
                                    val intent =
                                        Intent(applicationContext, TelaEscolhaRelatorio::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                } else {
                                    exibirDialogSenha(senha, TelaEscolhaRelatorio::class.java)
                                }
                            } else {
                                val intent =
                                    Intent(applicationContext, TelaEscolhaRelatorio::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }

                        }


                        R.id.menu_exportar_contagens -> {
                            val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                            val configuracoes = configuracaoDao.verificarConfiguracao()
                            val senha = configuracoes?.senhaAcesso.toString()

                            if (checarConfiguracoes()) {
                                if (senha == "99999") {
                                    val intent =
                                        Intent(
                                            applicationContext,
                                            TelaEscolhaLayoutExportacao::class.java
                                        )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                } else {
                                    exibirDialogSenha(
                                        senha,
                                        TelaEscolhaLayoutExportacao::class.java
                                    )
                                }
                            } else {
                                val intent =
                                    Intent(
                                        applicationContext,
                                        TelaEscolhaLayoutExportacao::class.java
                                    )
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }

                        }

                        R.id.desenvolvedor -> {
                            val intent =
                                Intent(applicationContext, TelaDesenvolvedor::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }

                        R.id.menu_excluir_banco -> {

                            val configuracaoDao = ConfiguracaoDAO.getInstance(applicationContext)
                            val configuracoes = configuracaoDao.verificarConfiguracao()
                            val senha = configuracoes?.senhaAcesso.toString()


                            if (checarConfiguracoes()) {
                                if (senha == "99999") {
                                    confirmarExclusaoBanco()
                                } else {
                                    exibirDialogrestaurarBkp(senha, { confirmarExclusaoBanco() })
                                }
                            } else {
                                confirmarExclusaoBanco()
                            }

                        }

                    }
                    return true
                }
            }
        )
    }

    private fun confirmarExclusaoBanco() {
        val builder = contexto?.let { AlertDialog.Builder(it) }
        builder?.setTitle("EXLUSÃO DE TODAS AS TABELAS")
        builder?.setMessage(
            "Se você escolher sim, será exluido todas as tabelas com" +
                    " todos os seus conteúdos.\n " +
                    "Esta ação não poderá ser desfeita"
        )
        builder?.setPositiveButton("Sim") { dialog, which ->
            exluirTodastabelas()
        }
        builder?.setNegativeButton("Não") { dialog, which ->
            exibirMensagem("Ação cancelada.")
        }

        builder?.create()?.show()
    }

    private fun analisarContagens() {
        val divergenciaDAO = contexto?.let { DivergenciaDAO.getInstance(it) }

        if (divergenciaDAO != null) {
            if (divergenciaDAO.deletarListaDivergencia()) { // Primeiro limpando a lista
                if (divergenciaDAO.compararDivergencia()) {
                    val intent =
                        Intent(
                            applicationContext,
                            TelaAnalisarDivergencia::class.java
                        )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    exibirMensagem("Não existem contagens para comparar")
                }
            }
        }
    }

    private fun receberContagens() {
        val receberContagensDAO = TransmitirReceberContagensDAO.getInstance(this)

        if (receberContagensDAO.receberContagens()) {
            exibirMensagem("Arquivos recebidos com sucesso.")
        } else {
            exibirMensagem("Erro ao receber arquivos.")
        }
    }

    private fun transmitirContagens() {
        val transmitirContagensDAO = TransmitirReceberContagensDAO.getInstance(this)

        if (transmitirContagensDAO.transmitirContagens()) {
            exibirMensagem("Arquivos gerados com sucesso.")
        } else {
            exibirMensagem("Erro ao gerar arquivos.")
        }

    }


    @SuppressLint("SuspiciousIndentation")
    private fun inicializarEventosClick() {
        binding.btnEntrarEnderecos.setOnClickListener {
            val intent = Intent(this, TelaListagemEnderecos::class.java)
            startActivity(intent)
        }
    }

    private fun selecionarBackupParaRestaurar() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
        }
        openDocumentLauncher.launch(intent)
    }

    private fun criarBackUp() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "backup.db")
        }
        createDocumentLauncher.launch(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val databaseHelper = DatabaseHelper(this)
                val sucesso = databaseHelper.gerarBackup(this, uri)

                if (sucesso) {
                    exibirMensagem("Backup salvo com sucesso: $uri")
                } else {
                    exibirMensagem("Erro ao gerar backup")
                }
            }
        }
    }

    private fun exluirTodastabelas() {

        val dropTudo = DROP_TABLES_DAO(this)

        if (dropTudo.apagarTodasTabelas()) {
            exibirMensagem("Todas as tabelas foram apagadas")
            val intent = Intent(this, TelaLogin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        } else {
            exibirMensagem("Erro ao excluir tabelas")
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeTelaPrincipal.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Tela principal"
            subtitle = "Escolha uma opção"
        }
    }

    private fun verificarCabecalhoTeste() {

        val contagemDAO = ContagemDAO.getInstance(this)
        listaItens = contagemDAO.listarItens()

    }

    override fun onResume() {
        super.onResume()
        alimentarDashBoardsEnderecos()
        configuracoesSistema()
    }

    override fun onRestart() {
        super.onRestart()
        alimentarDashBoardsEnderecos()
        configuracoesSistema()
    }

    private fun configuracoesSistema() {

        try {
            val configuracoesDAO = ConfiguracaoDAO.getInstance(this)
            val configuracoes = configuracoesDAO.verificarConfiguracao()

            if (configuracoes?.importarCadastro == true) {
                config = true
            } else {
                config = false
            }
        } catch (e: Exception) {
            exibirMensagem("Impossível checar configurações")
            Log.i("ROLA", "configuracoesSistema: ${e.message.toString()} ")
        }

    }


    @SuppressLint("InlinedApi")
    private fun verificarESolicitarPermissoes() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            // Todas as permissões já foram concedidas
            // Execute a lógica relacionada às permissões
            // Por exemplo, inicie uma tarefa que requer permissões concedidas
            iniciarTarefaQueRequerPermissoes()
        }
    }

    private fun iniciarTarefaQueRequerPermissoes() {
        // Implemente aqui a lógica para iniciar a tarefa que requer permissões concedidas
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                // Verificar se todas as permissões foram concedidas
                var allPermissionsGranted = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    }
                }

                if (allPermissionsGranted) {
                    // Todas as permissões foram concedidas
                    // Execute a lógica relacionada às permissões
                    // Por exemplo, inicie uma tarefa que requer permissões concedidas
                    iniciarTarefaQueRequerPermissoes()
                } else {
                    // Alguma permissão foi negada, lide com isso de acordo (por exemplo, mostre uma mensagem)

                }
            }
        }
    }

    private fun checarConfiguracoes(): Boolean {
        return try {
            val configuracoesDAO = ConfiguracaoDAO.getInstance(this)
            val configuracoes = configuracoesDAO.verificarConfiguracao()
            configuracoes != null // Retorna true se configuracoes não for null, caso contrário, retorna false
        } catch (e: Exception) {
            false // Retorna false se houver uma exceção
        }
    }

}