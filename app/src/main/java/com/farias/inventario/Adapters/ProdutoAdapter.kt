package com.farias.inventario.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.farias.inventario.Modelos.Produto
import com.farias.inventario.databinding.ItemProdutoBinding

class ProdutoAdapter(
    private val onClick: (Produto, tipo: Int) -> Unit,
    private val onLongClick: (id: Int) -> Unit
) : Adapter<ProdutoAdapter.ListagemProdutoViewHolder>() {

    private var listaProdutos = emptyList<Produto>()

    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(lista: List<Produto>) {
        listaProdutos = lista
        notifyDataSetChanged()
    }

    inner class ListagemProdutoViewHolder(
        private val binding: ItemProdutoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: Produto) {
            binding.txtCodigoProduto.text = produto.codigo_produto
            binding.txtCodigoAuxiliarProduto.text = produto.codigo_auxiliar_produto
            binding.txtDescricaoProdutoP.text = produto.descricao_produto

            binding.itemlListaProduto.setOnClickListener {
                onClick(produto, 1)
            }

            binding.itemlListaProduto.setOnLongClickListener {
                onLongClick(produto.id_produto)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: ListagemProdutoViewHolder, position: Int) {
        val produto = listaProdutos[position]
        holder.bind(produto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListagemProdutoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = ItemProdutoBinding.inflate(layoutInflater, parent, false)
        return ListagemProdutoViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listaProdutos.size
    }
}
