package com.farias.inventario.Activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.farias.inventario.BancoDados.ConfiguracaoDAO
import com.farias.inventario.R
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaConfiguracoesBinding

class TelaConfiguracoes : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaConfiguracoesBinding.inflate(layoutInflater)
    }

    private lateinit var spinner: Spinner
    private var delimitadorCampos: String = "Delimitador"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checarConfiguracoes()
        inicializarEventosClick()
        inicializarToolbar()
        inicializarSpinner("", 2)

    }


    private fun inicializarSpinner(delimitador: String, quemChamou: Int) {
        spinner = binding.spinerDelimitador
        var opcoesDelimitador: Array<String>

        if (quemChamou == 1) {
            opcoesDelimitador = arrayOf(delimitador)
        } else {
            opcoesDelimitador = arrayOf(
                "Delimitador",
                "Ponto e Virgula",
                "Tab",
                "Pipe",
                "Aspa simples",
                "Menor que",
                "Maior que"
            )
        }

        val delimitadoresMapIda = mapOf(
            "Ponto e Virgula" to ";",
            "Tab" to "\t",
            "Pipe" to "|",
            "Aspa simples" to "'",
            "Menor que" to "<",
            "Maior que" to ">"
        )

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            opcoesDelimitador
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                val nomeMostrado = opcoesDelimitador[position]
                (view as TextView).text = nomeMostrado
                view.textSize = 35f // Tamanho do texto para o item selecionado
                view.setTextColor(Color.BLACK)
                view.setBackgroundResource(R.drawable.shape_editi_text)
                val paddingInPixels = 5 //
                view.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels)

                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val textView = TextView(context)
                if (position == 0) {
                    // Se for o primeiro item (mensagem), retorna uma View vazia
                    textView.visibility = View.GONE
                } else {
                    val nomeClicado = opcoesDelimitador[position]
                    delimitadorCampos = nomeClicado
                    textView.text = nomeClicado
                    textView.textSize = 30f // Tamanho do texto para a lista suspensa
                    textView.setTextColor(Color.BLACK)

                    val paddingInPixels = 2 //
                    textView.setPadding(
                        paddingInPixels,
                        paddingInPixels,
                        paddingInPixels,
                        paddingInPixels
                    )
                    textView.gravity = Gravity.CENTER
                    textView.setBackgroundResource(R.drawable.shape_editi_text)
                }
                return textView
            }
        }
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) { // Verifica se não é o primeiro item (mensagem)
                    val nomeSelecionado = opcoesDelimitador[position]
                    delimitadorCampos = delimitadoresMapIda[nomeSelecionado].toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nada a fazer aqui
            }
        }
    }

    private fun inicializarEventosClick() {
        configurarCampos()
        binding.btnGravar.setOnClickListener {

            var codP = 0 //Principal
            var codA = 0 // Auxiliar
            var senha = "99999"

            val configuracaoDAO = ConfiguracaoDAO.getInstance(this)
            val usarBase = binding.switchBase.isChecked
            val usarCodPrincipal = binding.switchCodigoPrincipal.isChecked
            val usarCodAuxiliar = binding.switchCodigoAuxiliar.isChecked
            val usarDescricao = binding.switchDescricao.isChecked
            val senhaAcesso = binding.edtSenhaAcesso.text.trim().toString()

            if (usarBase) {
                if (delimitadorCampos != "" && delimitadorCampos != "Delimitador") {
                    if (usarCodPrincipal) {
                        val tamanhoCodPrincipal =
                            binding.edtitQtdCaracteresCodPrincipal.text.trim().toString()
                        if (tamanhoCodPrincipal.isNotEmpty()) {
                            val tamanhoCodPrincipalInteiro = tamanhoCodPrincipal.toInt()
                            if (tamanhoCodPrincipalInteiro <= 20) {
                                codP = tamanhoCodPrincipalInteiro
                                if (senhaAcesso.isNotEmpty()){
                                    senha = senhaAcesso
                                }else{
                                    exibirMensagem("A senha é um campo obrigatório.")
                                    return@setOnClickListener
                                }
                            } else {
                                exibirMensagem("Código principal deve ser menor ou igual a 20")
                                return@setOnClickListener
                            }
                        } else {
                            exibirMensagem("Informe a quantidade de caracteres para codigo principal")
                            return@setOnClickListener
                        }
                    } else {
                        exibirMensagem("Código principal obrigatório")
                        return@setOnClickListener
                    }
                } else {
                    exibirMensagem("Escolha um delimitador")
                    return@setOnClickListener
                }

                if (usarCodAuxiliar) {
                    val tamanhoCodAuxiliar =
                        binding.edtitQtdCaracteresCodAuxiliar.text.trim().toString()
                    if (tamanhoCodAuxiliar.isNotEmpty()) {
                        val tamanhoCodAuxiliarInteiro = tamanhoCodAuxiliar.toInt()
                        if (tamanhoCodAuxiliarInteiro <= 20) {
                            codA = tamanhoCodAuxiliarInteiro
                        } else {
                            exibirMensagem("Código auxiliar deve ser menor ou igual a 20")
                            return@setOnClickListener
                        }
                    } else {
                        exibirMensagem("Informe a quantidade de caracteres para codigo auxiliar!")
                        return@setOnClickListener
                    }
                }

                if (delimitadorCampos.isNotEmpty()) {

                    if (configuracaoDAO.deletarConfiguracoes()) {
                        val listaBooleanos =
                            listOf(usarBase, usarCodPrincipal, usarCodAuxiliar, usarDescricao)
                        if (configuracaoDAO.configuracoes(
                                listaBooleanos,
                                codP,
                                codA,
                                delimitadorCampos,
                                senha
                            )
                        ) {
                            tonarCamposNaoEditaveis()
                            binding.btnEditar.visibility = View.VISIBLE
                            binding.btnGravar.visibility = View.GONE
                            exibirMensagem("Configurado com sucesso")
                        } else {
                            exibirMensagem("Erro gravar dados no banco")
                        }
                    } else {
                        exibirMensagem("Erro ao deletar configurações antigas")
                    }
                } else {
                    exibirMensagem("Selecione o delimitador de campos")
                    return@setOnClickListener
                }
            } else {
                if (configuracaoDAO.deletarConfiguracoes()) {
                    if (delimitadorCampos != "" && delimitadorCampos != "Delimitador") {
                        val listaBooleanos =
                            listOf(usarBase, usarCodPrincipal, usarCodAuxiliar, usarDescricao)
                        if (configuracaoDAO.configuracoes(
                                listaBooleanos,
                                0,
                                0,
                                delimitadorCampos,
                                senha
                            )
                        ) {
                            exibirMensagem("Configurado com sucesso")
                            binding.btnEditar.visibility = View.VISIBLE
                            binding.btnGravar.visibility = View.GONE
                        } else {
                            exibirMensagem("Erro ao configurar")
                        }
                    } else {
                        exibirMensagem("Escolha um delimitador")
                        return@setOnClickListener
                    }
                } else {
                    exibirMensagem("Erro ao deletar configurações antigas")
                }
            }
        }

        binding.btnEditar.setOnClickListener {
            checarConfiguracoes()
            tonarCamposEditaveis()
            configurarCampos()
            inicializarSpinner("", 2)
            binding.btnEditar.visibility = View.GONE
            binding.btnGravar.visibility = View.VISIBLE

        }
    }

    private fun configurarCampos() {
        binding.switchBase.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.switchCodigoPrincipal.visibility = View.VISIBLE
                binding.switchCodigoAuxiliar.visibility = View.VISIBLE
                binding.switchDescricao.visibility = View.VISIBLE
                binding.txtCodPrincipalSwitch.visibility = View.VISIBLE
                binding.txtCodAuxiliarSwitch.visibility = View.VISIBLE
                binding.txtDescricaolSwitch.visibility = View.VISIBLE
                binding.btnGravar.visibility = View.VISIBLE

            } else {
                binding.switchCodigoPrincipal.visibility = View.GONE
                binding.switchCodigoAuxiliar.visibility = View.GONE
                binding.switchDescricao.visibility = View.GONE
                binding.txtCodPrincipalSwitch.visibility = View.GONE
                binding.txtCodAuxiliarSwitch.visibility = View.GONE
                binding.txtDescricaolSwitch.visibility = View.GONE
                binding.btnGravar.visibility = View.VISIBLE

            }
        }
        binding.switchCodigoPrincipal.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.edtitQtdCaracteresCodPrincipal.visibility = View.VISIBLE
                binding.edtitQtdCaracteresCodPrincipal.isEnabled = true
            } else {
                binding.edtitQtdCaracteresCodPrincipal.setText("")
                binding.edtitQtdCaracteresCodPrincipal.visibility = View.GONE
                binding.edtitQtdCaracteresCodPrincipal.isEnabled = false
            }
        }

        binding.switchCodigoAuxiliar.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.edtitQtdCaracteresCodAuxiliar.visibility = View.VISIBLE
                binding.edtitQtdCaracteresCodAuxiliar.isEnabled = true
            } else {
                binding.edtitQtdCaracteresCodAuxiliar.setText("")
                binding.edtitQtdCaracteresCodAuxiliar.visibility = View.GONE
                binding.edtitQtdCaracteresCodAuxiliar.isEnabled = false
            }
        }

        binding.switchDescricao.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.edtiDescricaoDica.visibility = View.VISIBLE
            } else {
                binding.edtiDescricaoDica.visibility = View.GONE
            }
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeTelaConfiguracoes.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Tela configurações"
            subtitle = "Configuração sistema"
        }
    }

    private fun checarConfiguracoes() {
        try {
            val configuracoesDAO = ConfiguracaoDAO.getInstance(this)
            val configuracoes = configuracoesDAO.verificarConfiguracao()
            if (configuracoes != null) {
                binding.btnEditar.visibility = View.VISIBLE
                binding.btnGravar.visibility = View.GONE
                val delimitadoresMapVolta = mapOf(
                    ";" to "Ponto e Virgula",
                    "\t" to "Tab",
                    "|" to "Pipe",
                    "'" to "Aspa simples",
                    "<" to "Menor que",
                    ">" to "Maior que"
                )

                val opcaoSpinerDelimitador = delimitadoresMapVolta[configuracoes.delimitador]
                if (opcaoSpinerDelimitador != null) {
                    inicializarSpinner(opcaoSpinerDelimitador, 1)
                }
                tonarCamposVisiveis()
                binding.switchBase.isChecked = configuracoes.importarCadastro
                binding.switchCodigoPrincipal.isChecked = configuracoes.codigoProduto
                binding.switchCodigoAuxiliar.isChecked = configuracoes.codigoAuxiliar
                binding.switchDescricao.isChecked = configuracoes.descricao
                binding.edtitQtdCaracteresCodPrincipal.setText(configuracoes.tamanhoCodigoProduto.toString())
                binding.edtitQtdCaracteresCodAuxiliar.setText(configuracoes.tamanhoCodigoAuxiliar.toString())
                binding.edtSenhaAcesso.setText(configuracoes.senhaAcesso)
                tonarCamposNaoEditaveis()
            }

        } catch (e: Exception) {
            exibirMensagem("Impossível recuperar configurações")
            Log.i("ROLA", "configuracoesSistema: ${e.message.toString()} ")
        }
    }

    private fun tonarCamposVisiveis() {
        binding.switchBase.visibility = View.VISIBLE
        binding.switchCodigoPrincipal.visibility = View.VISIBLE
        binding.switchCodigoAuxiliar.visibility = View.VISIBLE
        binding.switchDescricao.visibility = View.VISIBLE
        binding.txtCodPrincipalSwitch.visibility = View.VISIBLE
        binding.txtCodAuxiliarSwitch.visibility = View.VISIBLE
        binding.txtDescricaolSwitch.visibility = View.VISIBLE
        binding.spinerDelimitador.visibility = View.VISIBLE
        binding.edtitQtdCaracteresCodPrincipal.visibility = View.VISIBLE
        binding.edtitQtdCaracteresCodAuxiliar.visibility = View.VISIBLE
        binding.edtiDescricaoDica.visibility = View.VISIBLE

    }

    private fun tonarCamposNaoEditaveis() {
        binding.switchBase.isEnabled = false
        binding.switchCodigoPrincipal.isEnabled = false
        binding.switchCodigoAuxiliar.isEnabled = false
        binding.switchDescricao.isEnabled = false
        binding.txtCodPrincipalSwitch.isEnabled = false
        binding.txtCodAuxiliarSwitch.isEnabled = false
        binding.txtDescricaolSwitch.isEnabled = false
        binding.spinerDelimitador.isEnabled = false
        binding.edtitQtdCaracteresCodPrincipal.isEnabled = false
        binding.edtitQtdCaracteresCodAuxiliar.isEnabled = false
        binding.edtiDescricaoDica.isEnabled = false
        binding.edtSenhaAcesso.isEnabled = false
    }

    private fun tonarCamposEditaveis() {
        binding.switchBase.isEnabled = true
        binding.switchCodigoPrincipal.isEnabled = true
        binding.switchCodigoAuxiliar.isEnabled = true
        binding.switchDescricao.isEnabled = true
        binding.txtCodPrincipalSwitch.isEnabled = true
        binding.txtCodAuxiliarSwitch.isEnabled = true
        binding.txtDescricaolSwitch.isEnabled = true
        binding.spinerDelimitador.isEnabled = true
        binding.edtitQtdCaracteresCodPrincipal.isEnabled = true
        binding.edtitQtdCaracteresCodAuxiliar.isEnabled = true
        binding.edtiDescricaoDica.isEnabled = true
        binding.edtSenhaAcesso.isEnabled = true
    }

    override fun onStart() {
        super.onStart()
        checarConfiguracoes()
    }

    override fun onResume() {
        super.onResume()
        checarConfiguracoes()
    }
}