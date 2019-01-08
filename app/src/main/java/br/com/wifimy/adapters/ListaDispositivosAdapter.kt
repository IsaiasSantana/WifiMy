package br.com.wifimy.adapters

import android.annotation.SuppressLint
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import br.com.wifimy.R
import br.com.wifimy.adapters.viewholders.DispositivosViewHolder
import br.com.wifimy.adapters.viewholders.FooterViewHolder
import br.com.wifimy.extensoes.getStringEnderecoIp
import br.com.wifimy.extensoes.inflar
import br.com.wifimy.model.DetalhesConexaoHelper
import br.com.wifimy.model.DetalhesDispositivos
import java.util.*

private const val ROW_DISPOSITIVO = 0

private const val ROW_FOOTER = 1

private const val DELAY_ADICIONAR_ITENS: Long = 500

class ListaDispositivosAdapter(private val listaDispositivos: ArrayList<DetalhesDispositivos?> = ArrayList(),
                               private val detalhesConexaoHelper: DetalhesConexaoHelper,
                               private val listener: OnItemClick) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun atualizarListaDispositivos(listaDispositivos: List<DetalhesDispositivos?>) {
        removerTudo()
        Handler().postDelayed({
            this.listaDispositivos.addAll(listaDispositivos)
            notifyDataSetChanged()
        }, DELAY_ADICIONAR_ITENS)
    }

    fun removerTudo() {
        listaDispositivos.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ROW_DISPOSITIVO) {
            DispositivosViewHolder(parent.inflar(R.layout.row_dispositivos))
        } else {
            FooterViewHolder(parent.inflar(R.layout.footer_row))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val detalhesWifi = listaDispositivos[position]
        if (holder is DispositivosViewHolder) {
            holder.apply {
                detalhesWifi?.let {
                    fabricante?.text = detalhesWifi.nomeFabricante
                    ip?.text = detalhesWifi.ip
                    mac?.text = detalhesWifi.mac
                    tvContaDispositivo?.text = "${position + 1}"
                    val ipRouter = detalhesConexaoHelper.gateway.getStringEnderecoIp()
                    val ipDevice = detalhesConexaoHelper.enderecoIp.getStringEnderecoIp()
                    img?.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_dispositivos))
                    if (ipRouter == detalhesWifi.ip) {
                        val icRoteador = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_roteador)
                        img?.setImageDrawable(icRoteador)
                        fabricante?.setText(R.string.seu_roteador)
                    }
                    if (ipDevice == detalhesWifi.ip) {
                        fabricante?.setText(R.string.seu_dispositivo)
                    }
                    itemView.setOnClickListener { listener.onClickItem(detalhesWifi) }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (listaDispositivos[position] != null) ROW_DISPOSITIVO else ROW_FOOTER
    }

    interface OnItemClick {
        fun onClickItem(detalhesDispositivo: DetalhesDispositivos)
    }

    override fun getItemCount(): Int = listaDispositivos.size
}
