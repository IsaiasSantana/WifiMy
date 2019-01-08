package br.com.wifimy.presenters

import br.com.wifimy.contracts.BuscaDispositivosFragmentContract
import br.com.wifimy.model.DetalhesDispositivos
import br.com.wifimy.repositorios.FabricantesRepository
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter

@InjectViewState
class BuscaDispositivosFragmentPresenter : MvpPresenter<BuscaDispositivosFragmentContract.View>(),
        BuscaDispositivosFragmentContract.Presenter {
    private val repository = FabricantesRepository()

    override fun encontrarFabricantes(listaEnderecosIp: ArrayList<String>) {
        repository.buscarFabricantes(listaEnderecosIp, object : FabricantesRepository.OnListaFabricantesListener {
            override fun result(dispositivos: ArrayList<DetalhesDispositivos>) {
                viewState.mostrarListaDispositivosEncontrados(ArrayList(dispositivos))
                viewState.fecharProgressoScan()
            }
        })
    }
}