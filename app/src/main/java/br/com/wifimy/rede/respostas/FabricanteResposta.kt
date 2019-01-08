package br.com.wifimy.rede.respostas

import com.google.gson.annotations.SerializedName

data class FabricanteResponse(var company: String?,
                              @SerializedName("mac_prefix") var prefixoMAC: String?,
                              var address: String?,
                              var type: String?,
                              var country: String?)

fun FabricanteResponse.isPropriedadesNaoNulas(): Boolean {
    return company != null && prefixoMAC != null && address != null && type != null && country != null
}

class FabricanteResposta(var result: FabricanteResponse)