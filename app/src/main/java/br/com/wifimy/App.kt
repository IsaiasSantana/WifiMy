package br.com.wifimy

import android.app.Application
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import br.com.wifimy.database.FornecedoresDB
import br.com.wifimy.database.FornecedoresDBLocal
import com.fstyle.library.helper.AssetSQLiteOpenHelperFactory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        bancoDeDados = Room.databaseBuilder(applicationContext,
                FornecedoresDB::class.java,
                NOME_BANCO)
                .openHelperFactory(AssetSQLiteOpenHelperFactory())
                .addMigrations(object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        // nothing to do
                    }
                })
                .build()

        bancoPesquisasUsuario = Room.databaseBuilder(applicationContext,
                FornecedoresDBLocal::class.java,
                "fornecedores")
                .fallbackToDestructiveMigration()
                .build()
    }

    companion object {
        lateinit var bancoDeDados: FornecedoresDB
        lateinit var bancoPesquisasUsuario: FornecedoresDBLocal
        const val NOME_BANCO = "fabricantes_local.db"
    }
}