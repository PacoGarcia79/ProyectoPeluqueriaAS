package com.pacogarcia.proyectopeluqueria.modelos

import android.os.Parcel
import android.os.Parcelable

/**
 * Entidad usada para la gestión de los diferentes grupos en los que se engloban los productos.
 */
class ProductoGrupo() : Parcelable {
    var idProductoGrupo: Int?
    var nombreGrupo: String?
    var foto: String?

    constructor(parcel: Parcel) : this() {
        idProductoGrupo = parcel.readValue(Int::class.java.classLoader) as? Int
        nombreGrupo = parcel.readString()
        foto = parcel.readString()
    }

    constructor(
        idProductoGrupo: Int,
        nombre: String,
        foto: String
    ) : this() {
        this.idProductoGrupo = idProductoGrupo
        this.nombreGrupo = nombre
        this.foto = foto
    }


    init {
        idProductoGrupo = 0
        nombreGrupo = ""
        foto = ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(idProductoGrupo)
        parcel.writeString(nombreGrupo)
        parcel.writeString(foto)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductoGrupo> {
        override fun createFromParcel(parcel: Parcel): ProductoGrupo {
            return ProductoGrupo(parcel)
        }

        override fun newArray(size: Int): Array<ProductoGrupo?> {
            return arrayOfNulls(size)
        }
    }
}