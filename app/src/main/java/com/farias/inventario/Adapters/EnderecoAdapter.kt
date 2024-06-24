package com.farias.easycalls.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.farias.inventario.Modelos.Endereco
import com.farias.inventario.databinding.ItemEnderecoCadastroBinding

class EnderecoAdapter(
    val onClickExcluir: (endereco: String) -> Unit
    //val onClickExcluir: (Int) -> Unit
   // val onClickExcluirTudo: (Int) -> Unit,

    ): Adapter<EnderecoAdapter.EnderecoViewHolder>() {

    private var listaEnderecos = emptyList<Endereco>()
    @SuppressLint("NotifyDataSetChanged")
    fun adicionarEndereco(lista: List<Endereco>){
        listaEnderecos = lista
        notifyDataSetChanged()
    }

    inner class EnderecoViewHolder(
        private val binding: ItemEnderecoCadastroBinding
    ) : ViewHolder(binding.root){

        fun bind(endereco: Endereco) {
            binding.txtCodigoEndereco.text = endereco.codigo_endereco
            binding.txtNomeEndereco.text = endereco.nome_endereco
            binding.txtStatus.text = endereco.status

            binding.imgExcluir.setOnClickListener {
                onClickExcluir(endereco.codigo_endereco)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnderecoViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = ItemEnderecoCadastroBinding.inflate(layoutInflater, parent, false)
        return EnderecoViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: EnderecoViewHolder, position: Int) {
        val endereco = listaEnderecos[position]
        holder.bind(endereco)
    }

    override fun getItemCount(): Int {
        return listaEnderecos.size
    }
}