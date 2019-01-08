package br.com.wifimy.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import br.com.wifimy.database.dao.FabricanteDAOLocalAsset
import br.com.wifimy.model.FabricanteLocalAsset

@Database(entities = [FabricanteLocalAsset::class], version = 1, exportSchema = false)
abstract class FornecedoresDB : RoomDatabase() {

    abstract fun fabricanteDAO(): FabricanteDAOLocalAsset
}