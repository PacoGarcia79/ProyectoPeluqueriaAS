package com.pacogarcia.proyectopeluqueria.modelos

import android.os.Parcel
import android.os.Parcelable

/**
 * Entidad usada para la gestión de los horarios del establecimiento.
 */
class Horario() : Parcelable {
    var idHorario: Int?
    var hora: String?

    constructor(parcel: Parcel) : this() {
        idHorario = parcel.readValue(Int::class.java.classLoader) as? Int
        hora = parcel.readString()
    }

    init{
        idHorario = 0
        hora = ""
    }

    constructor(
        idHorario: Int,
        hora: String
    ) : this() {
        this.idHorario = idHorario
        this.hora = hora
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(idHorario)
        parcel.writeString(hora)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Horario> {
        override fun createFromParcel(parcel: Parcel): Horario {
            return Horario(parcel)
        }

        override fun newArray(size: Int): Array<Horario?> {
            return arrayOfNulls(size)
        }
    }


}