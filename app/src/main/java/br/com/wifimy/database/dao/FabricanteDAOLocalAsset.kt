package br.com.wifimy.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import br.com.wifimy.model.FabricanteLocalAsset

@Dao
interface FabricanteDAOLocalAsset {
    @Query("SELECT * FROM fabricante_local WHERE prefixo_mac LIKE :prefixoMac")
    fun getFabricante(prefixoMac: String): FabricanteLocalAsset?
}