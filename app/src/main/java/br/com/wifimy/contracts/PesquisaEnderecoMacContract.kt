package br.com.wifimy.contracts

import android.support.annotation.StringRes
import br.com.wifimy.model.Fabricante
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

interface PesquisaEnderecoMacContract {

    interface View : MvpView {
        @StateStrategyType(OneExecutionStateStrategy::class)
        fun bloquearBotaoPesquisar()

        @StateStrategyType(OneExecutionStateStrategy::class)
        fun liberarBotaoPesquisar()

        @StateStrategyType(OneExecutionStateStrategy::class)
        fun mostrarLoading()

        @StateStrategyType(OneExecutionStateStrategy::class)
        fun fecharLoading()

        @StateStrategyType(OneExecutionStateStrategy::class)
        fun mostrarLayoutDetalhesFabricante(fabricante: Fabricante)

        @StateStrategyType(OneExecutionStateStrategy::class)
        fun mostrarMensagemErro(@StringRes mensagem: Int)

        @StateStrategyType(OneExecutionStateStrategy::class)
        fun fecharLayoutDetalhesFabricante()
    }

    interface Presenter {
        fun pesquisarEnderecoMac(enderecoMac: String)
    }
}