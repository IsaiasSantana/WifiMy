package br.com.wifimy.contracts

import android.support.annotation.StringRes
import br.com.wifimy.model.DetalhesDispositivos
import br.com.wifimy.model.Fabricante
import com.arellomobile.mvp.MvpView

interface DetalhesFabricanteFragmentContract {

    interface View : MvpView {
        fun ajustarRowEnderecoMacErro(@StringRes mensagem: Int)

        fun ajustarRowEnderecoMac(mensagem: String)

        fun ajustarRowEnderecoIpErro(@StringRes mensagem: Int)

        fun ajustarRowEnderecoIp(mensagem: String)

        fun ajustarRowCompanhiaErro(@StringRes mensagem: Int)

        fun ajustarRowCompanhia(mensagem: String)

        fun setMensagemErroCampoEndereco()

        fun setMensagemErroCampoTipo()

        fun setMensagemErroCampoPais()

        fun esconderTituloIpMac()

        fun preecherCampos(fabricante: Fabricante)
    }

    interface Presenter {
        fun validaderDetalhesDispositivo(detalhesDispositivo: DetalhesDispositivos)

        fun buscarOutrasInformacoes(enderecoMAC: String)
    }
}