package br.com.wifimy.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "fabricante_local")
data class FabricanteLocalAsset(var company: String = "",
                                @SerializedName("mac_prefix")
                                @PrimaryKey
                                @ColumnInfo(name = "prefixo_mac") var prefixoMAC: String,
                                var address: String?,
                                var type: String?,
                                var country: String?)