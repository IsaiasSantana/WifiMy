package br.com.wifimy.adapters

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import br.com.wifimy.R
import br.com.wifimy.adapters.viewholders.PontosAcessoViewHolder
import br.com.wifimy.extensoes.*
import br.com.wifimy.model.FabricanteLocalAsset
import br.com.wifimy.repositorios.FabricantesRepository

private const val TOTAL_INTENSIDADES_SINAL = 5

class PontosAcessoAdapter(val listaAPs: ArrayList<ScanResult> = ArrayList()) :
        RecyclerView.Adapter<PontosAcessoViewHolder>() {

    private val repository = FabricantesRepository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PontosAcessoViewHolder {
        return PontosAcessoViewHolder(parent.inflar(R.layout.row_ponto_acesso))
    }

    override fun getItemCount() = listaAPs.size

    override fun onBindViewHolder(holder: PontosAcessoViewHolder, position: Int) {
        val scanResult = listaAPs[position]
        val pairIntensidadeSinal = getImgIntensidadeSinal(scanResult.level)
        val corTextoIntesidade = ContextCompat.getColor(holder.itemView.context,
                pairIntensidadeSinal.idColorIntensidade)
        holder.imgItensidadeSinal?.setImageResource(pairIntensidadeSinal.idIconeIntensidade)
        val stringFormat = holder.itemView.context.getString(R.string.placeholder_intensidade_wif)
        val formatAp = holder.itemView.context.getString(R.string.distancia_ponto_acesso)
        val formatCanal = holder.itemView.context.getString(R.string.canal_ap)
        val formatFreq = holder.itemView.context.getString(R.string.frequencia_canal)
        holder.tvIntensidadeSinal?.text = String.format(stringFormat, scanResult.level)
        holder.tvIntensidadeSinal?.setTextColor(corTextoIntesidade)
        holder.tvEnderecoMac?.text = scanResult.BSSID
        holder.tvTipoSegurancaRoteador?.text = scanResult.capabilities
        holder.tvSSID?.text = scanResult.SSID
        holder.tvDistanciaAP?.text = String.format(formatAp, scanResult.calcularDistanciaDoPontoDeAcesso().comUmaCasaDecimal())
        holder.tvCanalAP?.text = String.format(formatCanal, scanResult.converteFrequenciaParaNumeroCanal())
        holder.tvFrequenciaCanal?.text = String.format(formatFreq, scanResult.frequency)
        val larguraCanal = scanResult.getLarguraCanal()
        if (larguraCanal != -1) {
            val format = holder.itemView.context.getString(R.string.largura_canal)
            holder.tvLarguraCanal?.text = String.format(format, larguraCanal)
        } else {
            holder.tvLarguraCanal?.visibility = View.GONE
        }
        repository.buscarFabricanteNoBancoAsset(scanResult.BSSID, object : FabricantesRepository.OnListenerAsset {
            override fun erro() {
                holder.tvNomeModeloRoteador?.setText(R.string.modelo_desconhecido)
            }

            override fun sucesso(fabricante: FabricanteLocalAsset) {
                holder.tvNomeModeloRoteador?.text = fabricante.company
            }
        })
    }

    fun atualizarListaPontosAcesso(pontosAcesso: ArrayList<ScanResult>) {
        try {
            listaAPs.clear()
            notifyDataSetChanged()
            listaAPs.addAll(pontosAcesso)
            notifyDataSetChanged()
        } catch (e: Exception) {
            // sem ação.
        }
    }

    private class IntensidadeSinalPair(val idIconeIntensidade: Int, val idColorIntensidade: Int)

    private fun getImgIntensidadeSinal(intensidadeSinal: Int): IntensidadeSinalPair {
        return when (WifiManager.calculateSignalLevel(intensidadeSinal, TOTAL_INTENSIDADES_SINAL)) {
            0 -> IntensidadeSinalPair(R.drawable.ic_wifi_sinal_0, R.color.cor_wifi_sinal_0)
            1 -> IntensidadeSinalPair(R.drawable.ic_wifi_sinal_1, R.color.cor_wifi_sinal_1_2)
            2 -> IntensidadeSinalPair(R.drawable.ic_wifi_sinal_2, R.color.cor_wifi_sinal_1_2)
            3 -> IntensidadeSinalPair(R.drawable.ic_wifi_sinal_3, R.color.cor_wifi_sinal_3)
            4 -> IntensidadeSinalPair(R.drawable.ic_wifi_sinal_4, R.color.cor_wifi_sinal_4)
            else -> IntensidadeSinalPair(R.drawable.ic_wifi_sinal_0, R.color.cor_wifi_sinal_0)
        }
    }
}