package com.farias.inventario.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.farias.inventario.Modelos.Usuario
import com.farias.inventario.databinding.ItemUsuarioBinding

class UsuarioAdapter (
    private val onLongClick: (id: Int) -> Unit
): Adapter<UsuarioAdapter.ListagemUsuariosViewHolder>(){


    private var listaUsuarios = emptyList<Usuario>()

    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(lista: List<Usuario>){
        listaUsuarios = lista
        notifyDataSetChanged()
    }

    inner class ListagemUsuariosViewHolder(
        private val binding: ItemUsuarioBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(usuario: Usuario){

            binding.txtCodigoAcesso.setText(usuario.codigo_usuario.toString())
            binding.txtNomeDeUsuario.setText(usuario.nome_usuario)

            binding.itemUsuario.setOnLongClickListener {
                onLongClick(usuario.id_usuario)
                true
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsuarioAdapter.ListagemUsuariosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = ItemUsuarioBinding.inflate(layoutInflater, parent, false)
        return ListagemUsuariosViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: UsuarioAdapter.ListagemUsuariosViewHolder,
        position: Int
    ) {
        val usuario = listaUsuarios[position]
        holder.bind((usuario))
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

}