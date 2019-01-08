package br.com.wifimy.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import br.com.wifimy.model.Fabricante

@Dao
interface FabricanteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserirFabricante(fabricante: Fabricante)

    @Query("SELECT * FROM fabricante WHERE prefixo_mac LIKE :prefixoMac")
    fun getFabricante(prefixoMac: String): Fabricante?

    @Query("SELECT * FROM fabricante WHERE prefixo_mac IN (:enderecosMac)")
    fun getFabricantes(enderecosMac: List<String>): List<Fabricante>?
}