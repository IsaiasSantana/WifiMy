package br.com.wifimy.model

import android.os.Parcel
import android.os.Parcelable

data class DetalhesDispositivos(val ip: String, val mac: String, var nomeFabricante: String) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString(), parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ip)
        parcel.writeString(mac)
        parcel.writeString(nomeFabricante)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DetalhesDispositivos> {
        override fun createFromParcel(parcel: Parcel): DetalhesDispositivos {
            return DetalhesDispositivos(parcel)
        }

        override fun newArray(size: Int): Array<DetalhesDispositivos?> {
            return arrayOfNulls(size)
        }
    }
}