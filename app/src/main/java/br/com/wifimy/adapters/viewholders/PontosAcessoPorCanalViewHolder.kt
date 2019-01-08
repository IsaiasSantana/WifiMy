package br.com.wifimy.adapters.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.row_ponto_acesso_por_canal.view.*

class PontosAcessoPorCanalViewHolder(view: View?): RecyclerView.ViewHolder(view) {
    val canalTextView = view?.canalTextView
    val totalPontosAcessoTextView = view?.totalPontosAcessoTextView
    val divisor = view?.divisor
}