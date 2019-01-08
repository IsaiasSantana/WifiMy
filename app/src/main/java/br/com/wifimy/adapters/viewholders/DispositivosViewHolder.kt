package br.com.wifimy.adapters.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.row_dispositivos.view.*

class DispositivosViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    val ip = itemView?.tvEnderecoIp
    val mac = itemView?.tvEnderecoMac
    val fabricante = itemView?.tvFabricante
    val img = itemView?.imgAvatar
    val tvContaDispositivo = itemView?.tvContaDispositivo
}