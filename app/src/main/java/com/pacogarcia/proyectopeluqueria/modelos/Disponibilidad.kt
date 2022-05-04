package com.pacogarcia.proyectopeluqueria.modelos

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Entidad usada para la gestión de la habilitación y deshabilitación de los horarios del establecimiento
 * para cada uno de los empleados, teniendo en cuenta eventos como bajas, vacaciones o días de cierre.
 */
class Disponibilidad() : Parcelable {

    var idDisponibilidad: Int?
    var idUsuario: Int?
    var nombre: String?
    var idHorario: Int?
    var hora: String?
    var fecha_comienzo: Date?
    var fecha_fin: Date?

    constructor(parcel: Parcel) : this() {
        idDisponibilidad = parcel.readValue(Int::class.java.classLoader) as? Int
        idUsuario = parcel.readValue(Int::class.java.classLoader) as? Int
        nombre = parcel.readString()
        idHorario = parcel.readValue(Int::class.java.classLoader) as? Int
        hora = parcel.readString()
    }

    init {
        idDisponibilidad = 0
        idUsuario = 0
        nombre = ""
        idHorario = 0
        hora = ""
        fecha_comienzo = null
        fecha_fin = null
    }

    constructor(
        idDisponibilidad: Int,
        idUsuario: Int,
        nombre: String,
        idHorario: Int,
        hora: String,
        fecha_comienzo: Date,
        fecha_fin: Date
    ) : this() {
        this.idDisponibilidad = idDisponibilidad
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.idHorario = idHorario
        this.hora = hora
        this.fecha_comienzo = fecha_comienzo
        this.fecha_fin = fecha_fin
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(idDisponibilidad)
        parcel.writeValue(idUsuario)
        parcel.writeString(nombre)
        parcel.writeValue(idHorario)
        parcel.writeString(hora)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Disponibilidad> {
        override fun createFromParcel(parcel: Parcel): Disponibilidad {
            return Disponibilidad(parcel)
        }

        override fun newArray(size: Int): Array<Disponibilidad?> {
            return arrayOfNulls(size)
        }
    }
}
