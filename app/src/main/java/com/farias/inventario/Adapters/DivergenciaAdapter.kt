package com.farias.inventario.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.farias.inventario.Modelos.Divergencia
import com.farias.inventario.R
import com.farias.inventario.databinding.ItemDivergenciaBinding

class DivergenciaAdapter() : Adapter<DivergenciaAdapter.ListagemDivergenciasViewHolder>() {
    private var listaDivergencia = emptyList<Divergencia>()
    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(lista: List<Divergencia>) {
        listaDivergencia = lista
        notifyDataSetChanged()
    }

    inner class ListagemDivergenciasViewHolder(
        private val binding: ItemDivergenciaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(divergencia: Divergencia) {
            // Redefinir cores para o estado padrão
            resetColors()

            // Aplicar os dados da divergência aos TextViews
            binding.txtEndereco.text = divergencia.enderecoDivergencia
            binding.txtCodigoProdutoDivergencia.text = divergencia.codigoProdutoDivergencia
            binding.txtDescricaoProduto.text = divergencia.descricao

            if (divergencia.quantidadeContagem1 != 0) {
                binding.txtQtd1.text = divergencia.quantidadeContagem1.toString()
            } else {
                binding.txtQtd1.text = ""
            }
            if (divergencia.quantidadeContagem2 != 0) {
                binding.txtQtd2.text = divergencia.quantidadeContagem2.toString()
            } else {
                binding.txtQtd2.text = ""
            }
            if (divergencia.quantidadeContagem3 != 0) {
                binding.txtQtd3.text = divergencia.quantidadeContagem3.toString()
            } else {
                binding.txtQtd3.text = ""
            }

            val textViews = arrayOf(
                binding.txtEndereco,
                binding.txtCodigoProdutoDivergencia,
                binding.txtDescricaoProduto,
                binding.txtQtd1,
                binding.txtQtd2,
                binding.txtQtd3
            )

            // Aplicar cores alteradas se as condições forem atendidas
            applyColors(textViews)
        }

        private fun resetColors() {
            // Redefinir cores para o estado padrão
            val textViews = arrayOf(
                binding.txtEndereco,
                binding.txtCodigoProdutoDivergencia,
                binding.txtDescricaoProduto,
                binding.txtQtd1,
                binding.txtQtd2,
                binding.txtQtd3
            )

            val drawable: Drawable? = ResourcesCompat.getDrawable(
                itemView.resources,
                R.drawable.shape_editi_text,
                null
            )

            for (textView in textViews) {
                textView.setTextColor(Color.BLACK)
                textView.background = drawable
            }
        }

        private fun applyColors(textViews: Array<TextView>) {
            for (i in textViews.indices) {
                for (j in i + 1 until textViews.size) {
                    if (textViews[i].text == textViews[j].text && textViews[i].text.toString() != "0" && textViews[i].text.toString() != "") {
                        // Se as contagens forem iguais e diferentes de 0, mude a cor dos dois TextViews e a cor de fundo das Views
                        mudarCorDoTexto(textViews[i], Color.GREEN)
                        mudarCorDoTexto(textViews[j], Color.GREEN)

                        val drawable: Drawable? = ResourcesCompat.getDrawable(
                            itemView.resources,
                            R.drawable.shape_editi_text_verde,
                            null
                        )
                        if (drawable != null) {
                            textViews[i].background = drawable
                            textViews[j].background = drawable

                            textViews[i].setTextColor(Color.WHITE)
                            textViews[j].setTextColor(Color.WHITE)
                        }
                    }
                }
            }
        }


        private fun mudarCorDoTexto(textView: TextView, cor: Int) {
            // Mudar a cor do texto
            textView.setTextColor(cor)
        }

        private fun corPadrao(view: View, drawable: Drawable) {
            // Mudar a cor de fundo da View
            view.background = drawable
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DivergenciaAdapter.ListagemDivergenciasViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = ItemDivergenciaBinding.inflate(layoutInflater, parent, false)
        return ListagemDivergenciasViewHolder(itemView)

    }

    override fun onBindViewHolder(
        holder: DivergenciaAdapter.ListagemDivergenciasViewHolder,
        position: Int
    ) {
        val divergencia = listaDivergencia[position]
        holder.bind(divergencia)
    }

    override fun getItemCount(): Int {
        return listaDivergencia.size
    }

}