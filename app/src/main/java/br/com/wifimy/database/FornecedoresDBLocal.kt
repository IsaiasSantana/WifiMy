package br.com.wifimy.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import br.com.wifimy.database.dao.FabricanteDAO
import br.com.wifimy.model.Fabricante

@Database(entities = [Fabricante::class], version = 3, exportSchema = false)
abstract class FornecedoresDBLocal : RoomDatabase() {

    abstract fun fabricanteDAO(): FabricanteDAO
}