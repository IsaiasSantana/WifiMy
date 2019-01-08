package br.com.wifimy.presenters

import br.com.wifimy.R
import br.com.wifimy.contracts.DetalhesFabricanteFragmentContract
import br.com.wifimy.model.DetalhesDispositivos
import br.com.wifimy.model.Fabricante
import br.com.wifimy.model.FabricanteLocalAsset
import br.com.wifimy.model.Pais
import br.com.wifimy.repositorios.FabricantesRepository
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import org.json.JSONArray

@InjectViewState
class DetalhesFabricanteFragmentPresenter : MvpPresenter<DetalhesFabricanteFragmentContract.View>(),
        DetalhesFabricanteFragmentContract.Presenter {

    val listaPaises = ArrayList<Pais>()

    val repository = FabricantesRepository()

    override fun validaderDetalhesDispositivo(detalhesDispositivo: DetalhesDispositivos) {
        if (detalhesDispositivo.mac.isEmpty()) {
            viewState.ajustarRowEnderecoMacErro(R.string.indisponivel)
        } else {
            viewState.ajustarRowEnderecoMac(detalhesDispositivo.mac)
        }

        if (detalhesDispositivo.ip.isEmpty()) {
            viewState.ajustarRowEnderecoIpErro(R.string.indisponivel)
        } else {
            viewState.ajustarRowEnderecoIp(detalhesDispositivo.ip)
        }

        repository.buscarFabricanteNoBancoAsset(detalhesDispositivo.mac, object : FabricantesRepository.OnListenerAsset {
            override fun sucesso(fabricante: FabricanteLocalAsset) {
                viewState.ajustarRowCompanhia(fabricante.company)
            }

            override fun erro() {
                viewState.ajustarRowCompanhiaErro(R.string.endereco_mac_desconhecido)
            }
        })
    }

    fun getPais(sigla: String?, jsonPaises: String?): String? {
        if (sigla != null && jsonPaises != null) {
            if (listaPaises.isEmpty()) {
                val json = JSONArray(jsonPaises)
                for (i in 0 until json.length() - 1) {
                    val objeto = json.getJSONObject(i)
                    listaPaises.add(Pais(objeto.getString("nome"),
                            objeto.getString("sigla2")))
                }
            }
            listaPaises.forEach {
                if (it.sigla2 == sigla) {
                    return it.nome
                }
            }
        } else {
            return null
        }
        return null
    }


    override fun buscarOutrasInformacoes(enderecoMAC: String) {
        repository.buscarFabricanteBancoLocal(enderecoMAC, object : FabricantesRepository.OnListener {
            override fun sucesso(fabricante: Fabricante) {
                viewState.preecherCampos(fabricante)
            }

            override fun erro() {
                viewState.setMensagemErroCampoEndereco()
                viewState.setMensagemErroCampoTipo()
                viewState.setMensagemErroCampoPais()
            }
        })
    }
}