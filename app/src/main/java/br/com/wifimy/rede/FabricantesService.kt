package br.com.wifimy.rede

import br.com.wifimy.rede.respostas.FabricanteResposta
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface FabricantesService {

    @GET("./{mac}/json")
    fun buscarFabricante(@Path("mac") mac: String): Call<FabricanteResposta>

    companion object {
        private const val BASE_URL = "https://macvendors.co/api/"

        fun create(): FabricantesService {
            val gson = GsonBuilder()
                    .setDateFormat("dd-MM-yyyy'T'HH:mm:ssZ")
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

            return retrofit.create(FabricantesService::class.java)
        }
    }
}
