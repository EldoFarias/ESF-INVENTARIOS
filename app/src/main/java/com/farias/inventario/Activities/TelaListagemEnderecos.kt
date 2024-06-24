package com.farias.inventario.Activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.farias.easycalls.adapters.ListagemEnderecoAdapter
import com.farias.inventario.BancoDados.ContagemDAO
import com.farias.inventario.BancoDados.EnderecoDAO
import com.farias.inventario.Modelos.Endereco
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaListagemEnderecosBinding

class TelaListagemEnderecos : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaListagemEnderecosBinding.inflate(layoutInflater)
    }

    private lateinit var enderecoAdapter: ListagemEnderecoAdapter
    private var listaEnderecos = emptyList<Endereco>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        inicializarEventosClick()

        enderecoAdapter = ListagemEnderecoAdapter(
            { id -> passarTelaContagem(id)  },
            { endereco, numeroContagem -> excluirContagem(endereco, numeroContagem)}, // Contagem 1
            { endereco, numeroContagem -> excluirContagem(endereco, numeroContagem)}, // Contagem 2
            { endereco, numeroContagem -> excluirContagem(endereco, numeroContagem)}  // Contagem 3
        )
        binding.rvListagemEnderecos.layoutManager = LinearLayoutManager(this)
        binding.rvListagemEnderecos.addItemDecoration(
            DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL
            )
        )
        binding.rvListagemEnderecos.adapter = enderecoAdapter

    }

    private fun inicializarEventosClick() {
        binding.editAbrirEndereco.requestFocus()

        binding.btnAbrirEndereco.setOnClickListener {
            val endereco = binding.editAbrirEndereco.text.toString().trim()
            val enderecoFormatado = endereco.padStart(4, '0')

            abrirEndereco(endereco, enderecoFormatado)

        }

        binding.editAbrirEndereco.setOnFocusChangeListener { _, hasFocus ->

            if (!hasFocus){
                val endereco = binding.editAbrirEndereco.text.toString().trim()
                val enderecoFormatado = endereco.padStart(4, '0')
                abrirEndereco(endereco, enderecoFormatado)
            }
        }
    }

    private fun abrirEndereco(endereco: String, enderecoFormatado: String) {
        if (endereco.isNotEmpty() || endereco != ""){
            val contagemDAO = ContagemDAO.getInstance(this)
            if (contagemDAO.verificarEnderecoExiste(enderecoFormatado)){
                passarTelaContagem(endereco.toInt())
            }else{
                exibirMensagem("Endereço não cadastrado")
            }
        }else{
            exibirMensagem("Informe um endereço para iniciar")
        }
    }

    private fun passarTelaContagem(codigoEndereco: Int) {
        recuperarListaParaOutraTela(codigoEndereco)
    }

    private fun excluirContagem(endereco: Int, numeroContagem: Int){
        val contagemDAO = ContagemDAO.getInstance(this)
        val enderecoFormatado = endereco.toString().padStart(4, '0')
        val mensagemComplemento = "Deseja realmente apagar os dados abaixo?" +
                "\n\n" +
                "Contagem: ${numeroContagem}" +
                "\n" +
                "Endereço: ${enderecoFormatado}" +
                "\n\n" +
                "Estes dados serão apagados permanentemente." +
                "\n\n" +
                "Esta ação não poderá ser desfeita."

        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("ATENÇÃO")
        alertBuilder.setMessage("${mensagemComplemento}")
        alertBuilder.setPositiveButton("SIM") { _, _ ->

            if (contagemDAO.deletarContagem(enderecoFormatado, numeroContagem)) {
                atualizarListaEnderecos()
                exibirMensagem("Contagem ${numeroContagem} do endereço ${enderecoFormatado} foi deletado.")
            }else{
                exibirMensagem("Erro ao excluir as contagens.")
            }
        }
        alertBuilder.setNegativeButton("NÃO") { _, _ -> }
        alertBuilder.create().show()

    }
    // Na tela de destino
    fun recuperarListaParaOutraTela(endereco: Int) {

        val codigoFormatado = endereco.toString().padStart(4, '0')
        val contagemDAO = ContagemDAO.getInstance(this)

        val listaResultados = contagemDAO.verificarStatusEndereco(codigoFormatado)

        if (listaResultados.isEmpty()) {
            val intent = Intent(this, TelaContagem::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("codigo", endereco)
            binding.editAbrirEndereco.setText("")
            startActivity(intent)
        } else {
            val mensagem = formatarMensagem(listaResultados)
            val mensagemComplemento = "Deseja realizar uma nova contagem neste endereço?"

            val alertBuilder = AlertDialog.Builder(this)
            alertBuilder.setTitle("ATENÇÃO")
            alertBuilder.setMessage("${mensagem}\n${mensagemComplemento}")
            alertBuilder.setPositiveButton("SIM") { _, _ ->
                val intent = Intent(this, TelaContagem::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("codigo", endereco)
                binding.editAbrirEndereco.setText("")
                startActivity(intent)
            }
            alertBuilder.setNegativeButton("NÃO") { _, _ -> }
            alertBuilder.create().show()
        }
    }

    fun formatarMensagem(listaResultados: List<Pair<String, String>>): String {
        var mensagem = ""
        for ((status, contagem) in listaResultados) {
            mensagem += "Contagem: ${contagem} Status: ${status}\n"
        }
        return mensagem
    }


    override fun onStart() {
        super.onStart()
        atualizarListaEnderecos()

    }

    private fun atualizarListaEnderecos() {
        val enderecoDAO = EnderecoDAO.getInstance(this)
        listaEnderecos = enderecoDAO.listar()

        // Obter os resultados das contagens para todos os endereços
        val resultadosContagens = verificarStatusTodasContagens()

        // Atualizar a lista de endereços com base nos resultados das contagens
        val listaEnderecosAtualizada = listaEnderecos.map { endereco ->
            // Obter os resultados das contagens para este endereço
            val resultadosContagemEndereco = resultadosContagens[endereco.codigo_endereco]

            // Verificar se há resultados para este endereço e definir a visibilidade das imagens de contagem
            val exibirContagem1 = resultadosContagemEndereco?.first ?: false
            val exibirContagem2 = resultadosContagemEndereco?.second ?: false
            val exibirContagem3 = resultadosContagemEndereco?.third ?: false

            // Atualizar o modelo Endereco com as informações das contagens
            endereco.copy(
                exibirContagem1 = exibirContagem1,
                exibirContagem2 = exibirContagem2,
                exibirContagem3 = exibirContagem3
            )
        }

        // Atualizar a lista de endereços no adapter
        enderecoAdapter.atualizarLista(listaEnderecosAtualizada)
    }


    fun verificarStatusTodasContagens(): Map<String, Triple<Boolean, Boolean, Boolean>> {
        val resultadoContagens = mutableMapOf<String, Triple<Boolean, Boolean, Boolean>>()
        val contagemDAO = ContagemDAO.getInstance(this)

        // Iterar sobre todos os endereços
        for (endereco in listaEnderecos) {
            // Verificar o status da contagem para o endereço atual
            val contagens = contagemDAO.verificarStatusEndereco(endereco.codigo_endereco)

            // Processar os resultados para determinar se as imagens devem ser exibidas ou não
            val exibirContagem1 = contagens.any { it.second == "1" }
            val exibirContagem2 = contagens.any { it.second == "2" }
            val exibirContagem3 = contagens.any { it.second == "3" }

            // Armazenar os resultados processados
            resultadoContagens[endereco.codigo_endereco] = Triple(exibirContagem1, exibirContagem2, exibirContagem3)
        }

        return resultadoContagens
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeListagemEndereco.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Listagem de endereços"
            subtitle = "Inicie as contagens dos endereços"
            // setDisplayHomeAsUpEnabled(true)
        }
    }

}