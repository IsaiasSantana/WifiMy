package br.com.wifimy.contracts

import br.com.wifimy.model.DetalhesDispositivos
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

interface BuscaDispositivosFragmentContract {

    interface View : MvpView {
        @StateStrategyType(OneExecutionStateStrategy::class)
        fun mostrarListaDispositivosEncontrados(listaDispositivos: ArrayList<DetalhesDispositivos?>)

        @StateStrategyType(OneExecutionStateStrategy::class)
        fun mostrarProgressoScan()

        @StateStrategyType(OneExecutionStateStrategy::class)
        fun fecharProgressoScan()
    }

    interface Presenter {
        fun encontrarFabricantes(listaEnderecosIp: ArrayList<String>)
    }
}