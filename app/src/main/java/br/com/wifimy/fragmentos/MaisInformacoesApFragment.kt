package br.com.wifimy.fragmentos


import android.net.DhcpInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import br.com.wifimy.R
import br.com.wifimy.R.id.*
import br.com.wifimy.extensoes.*
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.banner_layout.*
import kotlinx.android.synthetic.main.mais_informacoes_ap_layout.*

private const val DHCP_INFO = "dhcpInfo"
private const val NOME_REDE = "nomeRede"

class MaisInformacoesApFragment : Fragment() {

    private lateinit var dhcpInfo: DhcpInfo

    private lateinit var nomeRede: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dhcpInfo = it.getParcelable(DHCP_INFO)
            nomeRede = it.getString(NOME_REDE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return container?.inflar(R.layout.fragment_mais_informacoes_ap, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializarNomeLabelsViews()
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun ajustaLabelsViews(view: View,
                                  labelNome: Int = R.string.traco,
                                  idView: Int,
                                  labelNomeString: String? = null) {
        view.setTextoTextView(idView, labelNomeString ?: getString(labelNome))
    }

    private fun inicializarNomeLabelsViews() {
        val formatoTempo = getString(R.string.tempo_concessao_d_h_m)
        val lDuration = dhcpInfo.leaseDuration.toLong() * 1000
        val strTempo = String.format(formatoTempo, lDuration.paraDias(), lDuration.paraHoras(), lDuration.paraMinutos())
        ajustaLabelsViews(rowNomeRede, R.string.ssid_rede, R.id.tvNomeLabel)
        ajustaLabelsViews(rowDns1, R.string.dns1, R.id.tvNomeLabel)
        ajustaLabelsViews(rowDns2, R.string.dns2, R.id.tvNomeLabel)
        ajustaLabelsViews(rowGateway, R.string.gateway, R.id.tvNomeLabel)
        ajustaLabelsViews(rowLeaseDuration, R.string.tempoConcessao, R.id.tvNomeLabel)
        ajustaLabelsViews(rowMascaraDeRede, R.string.mascara_rede, R.id.tvNomeLabel)
        ajustaLabelsViews(rowEnderecoServidor, R.string.endereco_servidor, R.id.tvNomeLabel)

        ajustaLabelsViews(rowNomeRede, labelNomeString = nomeRede, idView = R.id.tvConteudoLabel)
        ajustaLabelsViews(rowDns1, labelNomeString = dhcpInfo.dns1.getStringEnderecoIp(), idView = R.id.tvConteudoLabel)
        ajustaLabelsViews(rowDns2, labelNomeString = dhcpInfo.dns2.getStringEnderecoIp(), idView = R.id.tvConteudoLabel)
        ajustaLabelsViews(rowGateway, labelNomeString = dhcpInfo.gateway.getStringEnderecoIp(), idView = R.id.tvConteudoLabel)
        ajustaLabelsViews(rowLeaseDuration, labelNomeString = strTempo, idView = R.id.tvConteudoLabel)
        ajustaLabelsViews(rowMascaraDeRede, labelNomeString = dhcpInfo.netmask.getStringEnderecoIp(), idView = R.id.tvConteudoLabel)
        ajustaLabelsViews(rowEnderecoServidor, labelNomeString = dhcpInfo.serverAddress.getStringEnderecoIp(), idView = R.id.tvConteudoLabel)
    }

    companion object {
        const val TAG = "MaisInformacoesApFragment"
        @JvmStatic
        fun newInstance(dhcpInfo: DhcpInfo, nomeRede: String) =
                MaisInformacoesApFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(DHCP_INFO, dhcpInfo)
                        putString(NOME_REDE, nomeRede)
                    }
                }
    }
}
