package com.farias.easycalls.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.farias.inventario.Modelos.Endereco
import com.farias.inventario.databinding.ItemEnderecoContagemBinding

class ListagemEnderecoAdapter(
    val onClick: (endereco: Int) -> Unit,
    val onClickCont1: (endereco: Int, contagem1: Int) -> Unit,
    val onClickCont2: (endereco: Int, contagem2: Int) -> Unit,
    val onClickCont3: (endereco: Int, contagem3: Int) -> Unit
): Adapter<ListagemEnderecoAdapter.ListagemEnderecoViewHolder>() {

    private var listaEnderecos = emptyList<Endereco>()

    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(lista: List<Endereco>){
        listaEnderecos = lista
        notifyDataSetChanged()
    }

    inner class ListagemEnderecoViewHolder(
        private val binding: ItemEnderecoContagemBinding
    ) : ViewHolder(binding.root){

        fun bind(endereco: Endereco) {
            binding.txtCodigoEndereco.text = endereco.codigo_endereco
            binding.txtNomeEndereco.text = endereco.nome_endereco
            binding.txtStatus.text = endereco.status

            binding.imgCont1.visibility = if (endereco.exibirContagem1) View.VISIBLE else View.GONE
            binding.imgCont2.visibility = if (endereco.exibirContagem2) View.VISIBLE else View.GONE
            binding.imgCont3.visibility = if (endereco.exibirContagem3) View.VISIBLE else View.GONE


            binding.imgTelaEndereco.setOnClickListener {
                onClick(endereco.codigo_endereco.toInt())
            }
            binding.imgCont1.setOnLongClickListener {
                onClickCont1(endereco.codigo_endereco.toInt(), 1)
                true
            }
            binding.imgCont2.setOnLongClickListener {
                onClickCont2(endereco.codigo_endereco.toInt(),2)
                true
            }
            binding.imgCont3.setOnLongClickListener {
                onClickCont3(endereco.codigo_endereco.toInt(),3)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: ListagemEnderecoViewHolder, position: Int) {
        val endereco = listaEnderecos[position]
        holder.bind(endereco)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListagemEnderecoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = ItemEnderecoContagemBinding.inflate(layoutInflater, parent, false)
        return ListagemEnderecoViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listaEnderecos.size
    }
}
