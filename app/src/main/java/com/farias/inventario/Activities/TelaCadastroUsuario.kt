package com.farias.inventario.Activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.farias.inventario.Adapters.UsuarioAdapter
import com.farias.inventario.BancoDados.UsuarioDAO
import com.farias.inventario.Modelos.Usuario
import com.farias.inventario.Utilidades.DadosUsuarioLogado
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaCadastroUsuarioBinding

class TelaCadastroUsuario : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaCadastroUsuarioBinding.inflate(layoutInflater)
    }

    private var listaUsuarios = emptyList<Usuario>()

    private lateinit var usuariosAdapter: UsuarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        inicializarEventosClick()

        usuariosAdapter = UsuarioAdapter({id -> excluirUsuario(id)})

        binding.recycleViewUsuarios.layoutManager = LinearLayoutManager(this)
        binding.recycleViewUsuarios.adapter = usuariosAdapter

        atualizarListaUsuarios()

    }

    private fun atualizarListaUsuarios() {
        val usuarioDAO = UsuarioDAO.getInstance(this)
        listaUsuarios = usuarioDAO.listar()
        usuariosAdapter.atualizarLista(listaUsuarios)

    }

    private fun excluirUsuario(id: Int) {

        val alertBuilder = AlertDialog.Builder(this)

        alertBuilder.setTitle("Exclusão de Usuário")
        alertBuilder.setMessage("Deseja realmente excluir este usuário? \nEsta ação não poderá ser desfeita!!")
        alertBuilder.setPositiveButton("SIM") { _, _ ->
            val usuarioDAO = UsuarioDAO.getInstance(this)
            if (usuarioDAO.deletarUsuario(id)){
                exibirMensagem("Usuario deletado com sucesso.")
                atualizarListaUsuarios()
            } else {

            }
        }
        alertBuilder.setNegativeButton("NÃO") { _, _ -> }
        alertBuilder.create().show()

    }

    private fun inicializarEventosClick() {
        binding.btnCadastrarUsuario.setOnClickListener {

            val codigoAcesso = binding.edtCodigoAcesso.text.toString()
            val nomeusuario = binding.edtNome.text.toString()
            val senha1 = binding.edtSenhaCadastro.text.toString()
            val senha2 = binding.edtSenhaCadastroRepita.text.toString()

            if (codigoAcesso.isNotEmpty() && nomeusuario.isNotEmpty() && senha1.isNotEmpty() && senha2.isNotEmpty()) {
                if (senha1 == senha2) {
                    salvarUsuario(codigoAcesso.toInt(), nomeusuario, senha1)
                } else {
                    exibirMensagem("As senhas não são iguais")
                }
            } else {
                exibirMensagem("Todos os campos são obrigatórios")
            }
        }
    }

    private fun salvarUsuario(codigoAcesso: Int, nomeUsuario: String, senha1: String) {

        val usuarioDAO = UsuarioDAO(this)
        val usuario = Usuario(-1, codigoAcesso, nomeUsuario, senha1, "" )

        if (usuarioDAO.cadastrarUsuario(usuario)){

            val dadosUsuarioLogado = DadosUsuarioLogado.getInstance()
            dadosUsuarioLogado.codigoUsuario = codigoAcesso.toString()
            dadosUsuarioLogado.statusUsuario = "logado"

            limparCampos()
            atualizarListaUsuarios()
            //passarTelaPrincipal()
            exibirMensagem("Cadastro realizado com sucesso")
        }else{
            exibirMensagem("Erro ao cadastrar usuario")
        }
    }

    private fun passarTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
        finish()
    }

    private fun limparCampos() {
        binding.edtNome.setText("")
        binding.edtCodigoAcesso.setText("")
        binding.edtSenhaCadastro.setText("")
        binding.edtSenhaCadastroRepita.setText("")
    }


    private fun inicializarToolbar() {
        val toolbar = binding.includeCadastroUsuario.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Cadastro de usuário"
            subtitle = "Criação de conta local"
            // setDisplayHomeAsUpEnabled(true)
        }
    }

}