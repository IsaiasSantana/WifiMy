package br.com.wifimy.rede

object ApiManager {

    val fabricantesService by lazy {
        FabricantesService.create()
    }
}
