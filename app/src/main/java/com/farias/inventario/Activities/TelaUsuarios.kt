package com.farias.inventario.Activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.farias.inventario.Adapters.UsuarioAdapter
import com.farias.inventario.BancoDados.EnderecoDAO
import com.farias.inventario.BancoDados.UsuarioDAO
import com.farias.inventario.Modelos.Usuario
import com.farias.inventario.R
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaUsuariosBinding

class TelaUsuarios : AppCompatActivity() {

    private lateinit var usuarioAdapter: UsuarioAdapter
    private var listaUsuarios = emptyList<Usuario>()

    private val binding by lazy {
        ActivityTelaUsuariosBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        inicializarEventosClick()

        usuarioAdapter = UsuarioAdapter(
            { id -> excluirUsuario(id) }
        )

        binding.rvUsuarios.layoutManager = LinearLayoutManager(this)
        binding.rvUsuarios.adapter = usuarioAdapter

    }

    private fun inicializarEventosClick() {
        binding.btnExcluirTodosUsuarios.setOnClickListener {

            confirmarExclusao()

        }
    }

    private fun confirmarExclusao() {
        val alertBuilder = AlertDialog.Builder(this)

        alertBuilder.setTitle("ATENÇÃO, TODOS USUARIOS SERÃO EXCLUÍDOS")
        alertBuilder.setMessage("Deseja realmente excluir todos os usuários, esta ação não poderá ser desfeita")
        alertBuilder.setPositiveButton("SIM"){_,_ ->

            val usuarioDAO = UsuarioDAO(this)
            if (usuarioDAO.deletarTodosUsuarios()) {
                exibirMensagem("Todos os usuarios foram excluidos com sucesso")
                val intent = Intent(this, TelaLogin::class.java)
                startActivity(intent)
                finish()
            } else {
                exibirMensagem("Erro, não é possível exluir todos os usuários")
            }
        }
        alertBuilder.setNegativeButton("NÃO"){_,_ -> }
        alertBuilder.create().show()
    }

    private fun excluirUsuario(id: Int) {
        val alertBuilder = AlertDialog.Builder(this)

        alertBuilder.setTitle("Exclusão de usuário")
        alertBuilder.setMessage("Deseja realmente excluir este usuário, esta ação não poderá ser desfeita")
        alertBuilder.setPositiveButton("SIM"){_,_ ->

            val usuarioDAO = UsuarioDAO(this)
            if (usuarioDAO.deletarUsuario(id)) {
                exibirMensagem("Usuario excluído com sucesso")
                atualizarListaUsuarios()
            } else {
                exibirMensagem("Erro ao excluir usuário")
            }
        }
        alertBuilder.setNegativeButton("NÃO"){_,_ -> }
        alertBuilder.create().show()

    }

    override fun onStart() {
        super.onStart()
        atualizarListaUsuarios()
    }

    private fun atualizarListaUsuarios() {
        val usuarioDAO = UsuarioDAO(this)
        listaUsuarios = usuarioDAO.listar()
        usuarioAdapter.atualizarLista(listaUsuarios)

    }


    private fun inicializarToolbar() {
        val toolbar = binding.includeUsuarios.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Lista de usuários"
            subtitle = "Lista de usuários cadastrados"
        }
    }
}