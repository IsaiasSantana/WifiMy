package br.com.wifimy.presenters

import br.com.wifimy.R
import br.com.wifimy.contracts.PesquisaEnderecoMacContract
import br.com.wifimy.model.Fabricante
import br.com.wifimy.repositorios.FabricantesRepository
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter

@InjectViewState
class PesquisaEnderecoMacPresenter : MvpPresenter<PesquisaEnderecoMacContract.View>(),
        PesquisaEnderecoMacContract.Presenter {

    private val repository = FabricantesRepository()

    override fun pesquisarEnderecoMac(enderecoMac: String) {
        if (enderecoMac.isEmpty()) {
            viewState.mostrarMensagemErro(R.string.erro_endereco_mac_vazio)
            return
        }

        viewState.fecharLayoutDetalhesFabricante()
        viewState.mostrarLoading()
        viewState.bloquearBotaoPesquisar()
        repository.buscarFabricanteBancoLocal(enderecoMac, object : FabricantesRepository.OnListener {
            override fun sucesso(fabricante: Fabricante) {
                viewState.fecharLoading()
                viewState.mostrarLayoutDetalhesFabricante(fabricante = fabricante)
                viewState.liberarBotaoPesquisar()
            }

            override fun erro() {
                viewState.fecharLoading()
                viewState.liberarBotaoPesquisar()
                viewState.mostrarMensagemErro(R.string.erro_mac_nao_encontrado)
            }
        })
    }
}