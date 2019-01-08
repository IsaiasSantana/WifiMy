package br.com.wifimy.adapters.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.row_ponto_acesso.view.*

class PontosAcessoViewHolder(view: View?) : RecyclerView.ViewHolder(view) {
    val tvEnderecoMac = view?.tvEndMac
    val tvSSID = view?.tvSSID
    val tvNomeModeloRoteador = view?.tvNomeModeloRoteador
    val tvTipoSegurancaRoteador = view?.tvTipoSegurancaRoteador
    val imgCadeado = view?.imgCadeado
    val imgItensidadeSinal = view?.imgIntensidadeSinal
    val tvIntensidadeSinal = view?.tvIntensidadeSinal
    val tvDistanciaAP = view?.tvDistanciaAP
    val tvCanalAP = view?.tvCanalAP
    val tvLarguraCanal = view?.tvLarguraCanal
    val tvFrequenciaCanal = view?.tvFrequenciaCanal
}