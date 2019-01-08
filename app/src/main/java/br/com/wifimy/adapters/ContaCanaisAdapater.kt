package br.com.wifimy.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import br.com.wifimy.R
import br.com.wifimy.adapters.viewholders.PontosAcessoPorCanalViewHolder
import br.com.wifimy.extensoes.inflar

class ContaCanaisAdapater(private val pontosAcessoLista: ArrayList<Pair<String, String>> = ArrayList(),
                          private val callback: (() -> Unit)? = null):
        RecyclerView.Adapter<PontosAcessoPorCanalViewHolder>() {

    fun atualizar(pontosAcessoLista: List<Pair<String, String>>) {
        this.pontosAcessoLista.clear()
        notifyDataSetChanged()
        this.pontosAcessoLista.addAll(pontosAcessoLista)
        notifyDataSetChanged()
        callback?.let { it() }
    }

    fun limpar() {
        this.pontosAcessoLista.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PontosAcessoPorCanalViewHolder {
        return PontosAcessoPorCanalViewHolder(parent.inflar(R.layout.row_ponto_acesso_por_canal))
    }

    override fun getItemCount() = pontosAcessoLista.size

    override fun onBindViewHolder(holder: PontosAcessoPorCanalViewHolder, position: Int) {
        holder.divisor?.visibility = View.GONE
        holder.canalTextView?.text = pontosAcessoLista[position].first
        holder.totalPontosAcessoTextView?.text = pontosAcessoLista[position].second
    }
}