package com.farias.inventario.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.farias.inventario.Modelos.Itens
import com.farias.inventario.databinding.ItemContadoBinding

class ItensAdapter (
    private val onLongClick: (id: Int) -> Unit

) : Adapter<ItensAdapter.ListagemItensViewHolder>(){

    private var listaItens = emptyList<Itens>()

    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(lista: List<Itens>){
        listaItens = lista
        notifyDataSetChanged()
    }

    inner class ListagemItensViewHolder(
        private val binding: ItemContadoBinding
    ):RecyclerView.ViewHolder(binding.root){

        fun bind(itemContado: Itens){

            binding.txtCodigoItem.text = "${itemContado.codigo_produto}"
            binding.txtDescricaoItem.text = "${itemContado.descricao_produto}"
            binding.txtQuantidadeItem.text = "${itemContado.quantidade}"

            binding.itemContado.setOnLongClickListener {
                onLongClick(itemContado.id_item)
                true
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItensAdapter.ListagemItensViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = ItemContadoBinding.inflate(layoutInflater, parent, false)
        return ListagemItensViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItensAdapter.ListagemItensViewHolder, position: Int) {
        val item = listaItens[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return listaItens.size
    }
}