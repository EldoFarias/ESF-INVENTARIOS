package com.farias.inventario.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.farias.inventario.Modelos.ErroImportacaoProduto
import com.farias.inventario.databinding.ItemProdutoErroBinding

class ProdutosErrosImportacaoAdapter() :
    Adapter<ProdutosErrosImportacaoAdapter.ListagemProdutoViewHolder>() {

    private var listaProdutos = emptyList<ErroImportacaoProduto>()

    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(lista: List<ErroImportacaoProduto>) {
        listaProdutos = lista
        notifyDataSetChanged()
    }

    inner class ListagemProdutoViewHolder(
        private val binding: ItemProdutoErroBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: ErroImportacaoProduto) {

            binding.txtNumeroLinha.text = produto.linha_erro.toString()
            binding.txtCodigo.text = produto.codigo_produto
            binding.txtData.text = produto.data_cadastro

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListagemProdutoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = ItemProdutoErroBinding.inflate(layoutInflater, parent, false)
        return ListagemProdutoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListagemProdutoViewHolder, position: Int) {
        val produto = listaProdutos[position]
        holder.bind(produto)
    }

    override fun getItemCount(): Int {
        return listaProdutos.size
    }
}
