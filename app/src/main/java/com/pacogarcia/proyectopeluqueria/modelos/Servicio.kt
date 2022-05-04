package com.pacogarcia.proyectopeluqueria.modelos

import android.os.Parcel
import android.os.Parcelable

/**
 * Entidad usada para la gestión de los servicios que ofrece el establecimiento.
 */
class Servicio() : Parcelable {

    var idServicio: Int?
    var nombre: String?
    var precio: Double?
    var foto: String?

    constructor(parcel: Parcel) : this() {
        idServicio = parcel.readValue(Int::class.java.classLoader) as? Int
        nombre = parcel.readString()
        precio = parcel.readValue(Double::class.java.classLoader) as? Double
        foto = parcel.readString()
    }

    init {
        idServicio = 0
        nombre = ""
        precio= 0.0
        foto = ""
    }

    constructor(
        nombre: String,
        precio: Double,
        foto: String
    ) : this() {
        this.nombre = nombre
        this.precio = precio
        this.foto = foto
    }

    constructor(
        idServicio: Int,
        nombre: String,
        precio: Double,
        foto: String
    ) : this() {
        this.idServicio = idServicio
        this.nombre = nombre
        this.precio = precio
        this.foto = foto
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(idServicio)
        parcel.writeString(nombre)
        parcel.writeValue(precio)
        parcel.writeString(foto)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Servicio> {
        override fun createFromParcel(parcel: Parcel): Servicio {
            return Servicio(parcel)
        }

        override fun newArray(size: Int): Array<Servicio?> {
            return arrayOfNulls(size)
        }
    }


}