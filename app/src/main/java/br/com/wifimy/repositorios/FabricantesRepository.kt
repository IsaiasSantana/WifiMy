package br.com.wifimy.repositorios

import android.os.AsyncTask
import br.com.wifimy.App
import br.com.wifimy.extensoes.prefixoEnderecoMAC
import br.com.wifimy.model.DetalhesDispositivos
import br.com.wifimy.model.Fabricante
import br.com.wifimy.model.FabricanteLocalAsset
import br.com.wifimy.rede.ApiManager
import br.com.wifimy.rede.respostas.FabricanteResposta
import br.com.wifimy.rede.respostas.isPropriedadesNaoNulas
import com.crashlytics.android.Crashlytics
import com.stealthcopter.networktools.ARPInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class FabricantesRepository {

    fun buscarFabricantes(listaEnderecosIp: ArrayList<String>, listener: OnListaFabricantesListener) {
        BuscarFabricantes(listaEnderecosIp, object : OnListaFabricantesListener {
            override fun result(dispositivos: ArrayList<DetalhesDispositivos>) {
                listener.result(dispositivos)
            }
        }).execute()
    }

    fun buscarFabricanteNoBancoAsset(enderecoMac: String, listener: OnListenerAsset) {
        BuscarFabricanteNoBancoAsset(enderecoMac.prefixoEnderecoMAC(), object : OnListenerAsset {
            override fun sucesso(fabricante: FabricanteLocalAsset) {
                listener.sucesso(fabricante)
            }

            override fun erro() {
                buscarFabricanteBancoLocal(enderecoMac, object : OnListener {
                    override fun sucesso(fabricante: Fabricante) {
                        val fabricanteLocalAsset = FabricanteLocalAsset(company = fabricante.company,
                                prefixoMAC = fabricante.prefixoMAC,
                                address = fabricante.address,
                                type = fabricante.type,
                                country = fabricante.country)
                        listener.sucesso(fabricanteLocalAsset)
                    }

                    override fun erro() {
                        listener.erro()
                    }
                })
            }
        }).execute()
    }

    fun buscarFabricanteBancoLocal(enderecoMac: String, listener: OnListener) {
        BuscarFabricanteLocal(enderecoMac.prefixoEnderecoMAC(), object : OnListener {
            override fun sucesso(fabricante: Fabricante) {
                listener.sucesso(fabricante)
            }

            override fun erro() {
                buscarFabricanteServidor(enderecoMac, listener)
            }
        }).execute()
    }

    private fun buscarFabricanteServidor(mac: String, listener: OnListener) {
        ApiManager.fabricantesService.buscarFabricante(mac)
                .enqueue(object : Callback<FabricanteResposta> {
                    override fun onResponse(call: Call<FabricanteResposta>?, response: Response<FabricanteResposta>?) {
                        response?.body()?.result?.let {
                            if (it.isPropriedadesNaoNulas()) {
                                val fabricante = Fabricante(it.company!!,
                                        it.prefixoMAC!!.toLowerCase(Locale.FRANCE),
                                        it.address,
                                        it.type,
                                        it.country)
                                listener.sucesso(fabricante)
                                InserirFabricante(fabricante).execute()
                            } else {
                                listener.erro()
                            }
                            return
                        }
                        listener.erro()
                    }

                    override fun onFailure(call: Call<FabricanteResposta>?, t: Throwable?) {
                        listener.erro()
                    }
                })
    }

    private class InserirFabricante(val fabricante: Fabricante) : AsyncTask<Void, Void, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                App.bancoPesquisasUsuario.fabricanteDAO().inserirFabricante(fabricante)
            } catch (e: IllegalStateException) {
                Crashlytics.logException(e)
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }
            return null
        }
    }

    private class BuscarFabricanteNoBancoAsset(val prefixoMac: String, val listener: OnListenerAsset) :
            AsyncTask<Void, Void, FabricanteLocalAsset?>() {
        override fun doInBackground(vararg params: Void?): FabricanteLocalAsset? {
            return try {
                App.bancoDeDados.fabricanteDAO().getFabricante(prefixoMac)
            } catch (e: Exception) {
                Crashlytics.logException(e)
                null
            }
        }

        override fun onPostExecute(result: FabricanteLocalAsset?) {
            super.onPostExecute(result)
            result?.let {
                it.prefixoMAC = it.prefixoMAC.toLowerCase(Locale.FRANCE)
                listener.sucesso(it)
                return
            }
            listener.erro()
        }
    }

    private class BuscarFabricanteLocal(val prefixoMac: String, val listener: OnListener) :
            AsyncTask<Void, Void, Fabricante?>() {
        override fun doInBackground(vararg params: Void?): Fabricante? {
            return try {
                App.bancoPesquisasUsuario.fabricanteDAO().getFabricante(prefixoMac)
            } catch (e: Exception) {
                Crashlytics.logException(e)
                null
            }
        }

        override fun onPostExecute(result: Fabricante?) {
            super.onPostExecute(result)
            result?.let {
                it.prefixoMAC = it.prefixoMAC.toLowerCase(Locale.FRANCE)
                listener.sucesso(it)
                return
            }
            listener.erro()
        }
    }

    private class BuscarFabricantes(val enderecosIp: List<String>, val listener: OnListaFabricantesListener) :
            AsyncTask<Void, Void, ArrayList<DetalhesDispositivos>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<DetalhesDispositivos> {
            val listaDispositivos = ArrayList<DetalhesDispositivos>()
            try {
                for (ip in enderecosIp) {
                    val mac = ARPInfo.getMACFromIPAddress(ip)
                    val detalheDisp = DetalhesDispositivos(ip = ip, mac = mac
                            ?: "", nomeFabricante = "")
                    val fabricante = App.bancoDeDados.fabricanteDAO().getFabricante(detalheDisp.mac.prefixoEnderecoMAC())
                    fabricante?.let {
                        detalheDisp.nomeFabricante = it.company
                    }
                    listaDispositivos.add(detalheDisp)
                }
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }
            return listaDispositivos
        }

        override fun onPostExecute(result: ArrayList<DetalhesDispositivos>) {
            super.onPostExecute(result)
            listener.result(result)
        }
    }

    interface OnListaFabricantesListener {
        fun result(dispositivos: ArrayList<DetalhesDispositivos>)
    }

    interface OnListener {
        fun sucesso(fabricante: Fabricante)

        fun erro()
    }

    interface OnListenerAsset {
        fun sucesso(fabricante: FabricanteLocalAsset)

        fun erro()
    }
}