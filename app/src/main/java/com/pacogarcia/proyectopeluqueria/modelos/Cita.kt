package com.pacogarcia.proyectopeluqueria.modelos

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Entidad usada para la gestión de citas
 */
class Cita() : Parcelable {
    var idCliente: Int?
    var idHorarioEmpleado: Int?
    var idCita: Int?
    var fecha: Date?
    var cliente: String?
    var profesional: String?
    var nombre: String?
    var hora: String?
    var servicios: String?
    var precio_servicios: Double?
    var productos: String?
    var cantidad: String?
    var precio_productos: Double?
    var cancelada: Boolean?
    var telefono: String?


    init {
        idCliente = 0
        idHorarioEmpleado = 0
        idCita = 0
        fecha = null
        cliente = ""
        profesional = ""
        nombre = ""
        hora = ""
        servicios = ""
        precio_servicios = 0.0
        productos = ""
        cantidad = ""
        precio_productos = 0.0
        cancelada = false
        telefono = ""
    }

    constructor(
        idCliente: Int,
        idHorarioEmpleado: Int,
        fecha: Date,
        cancelada: Boolean
    ) : this() {
        this.idCliente = idCliente
        this.idHorarioEmpleado = idHorarioEmpleado
        this.fecha = fecha
        this.cancelada = cancelada
    }


    constructor(
        idCita: Int,
        fecha: Date,
        cliente: String,
        profesional: String,
        hora: String,
        servicios: String,
        precio_servicios: Double,
        productos: String,
        cantidad: String,
        precio_Productos: Double
    ) : this() {
        this.idCita = idCita
        this.fecha = fecha
        this.cliente = cliente
        this.profesional = profesional
        this.hora = hora
        this.servicios = servicios
        this.precio_servicios = precio_servicios
        this.productos = productos
        this.cantidad = cantidad
        this.precio_productos = precio_Productos
    }

    constructor(
        idCita: Int,
        fecha: Date,
        nombre: String,
        hora: String,
        servicios: String,
        precio_servicios: Double,
        productos: String,
        cantidad: String,
        precio_Productos: Double
    ) : this() {
        this.idCita = idCita
        this.fecha = fecha
        this.nombre = nombre
        this.hora = hora
        this.servicios = servicios
        this.precio_servicios = precio_servicios
        this.productos = productos
        this.cantidad = cantidad
        this.precio_productos = precio_Productos
    }

    constructor(
        idCita: Int,
        fecha: Date,
        nombre: String,
        hora: String,
        servicios: String,
        precio_servicios: Double,
        productos: String,
        cantidad: String,
        precio_Productos: Double,
        telefono: String
    ) : this() {
        this.idCita = idCita
        this.fecha = fecha
        this.nombre = nombre
        this.hora = hora
        this.servicios = servicios
        this.precio_servicios = precio_servicios
        this.productos = productos
        this.cantidad = cantidad
        this.precio_productos = precio_Productos
        this.telefono = telefono
    }

    constructor(
        idCliente: Int,
        fecha: Date,
        cliente: String,
        profesional: String,
        hora: String,
        servicios: String,
        precio_servicios: Double,
        productos: String,
        precio_Productos: Double
    ) : this() {
        this.idCliente = idCliente
        this.fecha = fecha
        this.cliente = cliente
        this.profesional = profesional
        this.hora = hora
        this.servicios = servicios
        this.precio_servicios = precio_servicios
        this.productos = productos
        this.precio_productos = precio_Productos
    }

    constructor(parcel: Parcel) : this() {
        idCliente = parcel.readValue(Int::class.java.classLoader) as? Int
        idHorarioEmpleado = parcel.readValue(Int::class.java.classLoader) as? Int
        idCita = parcel.readValue(Int::class.java.classLoader) as? Int
        fecha = parcel.readSerializable() as Date?
        cliente = parcel.readString()
        profesional = parcel.readString()
        nombre = parcel.readString()
        hora = parcel.readString()
        servicios = parcel.readString()
        precio_servicios = parcel.readValue(Double::class.java.classLoader) as? Double
        productos = parcel.readString()
        cantidad = parcel.readString()
        precio_productos = parcel.readValue(Double::class.java.classLoader) as? Double
        cancelada = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        telefono = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(idCliente)
        parcel.writeValue(idHorarioEmpleado)
        parcel.writeValue(idCita)
        parcel.writeSerializable(fecha)
        parcel.writeString(cliente)
        parcel.writeString(profesional)
        parcel.writeString(nombre)
        parcel.writeString(hora)
        parcel.writeString(servicios)
        parcel.writeValue(precio_servicios)
        parcel.writeString(productos)
        parcel.writeString(cantidad)
        parcel.writeValue(precio_productos)
        parcel.writeValue(cancelada)
        parcel.writeString(telefono)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Cita> {
        override fun createFromParcel(parcel: Parcel): Cita {
            return Cita(parcel)
        }

        override fun newArray(size: Int): Array<Cita?> {
            return arrayOfNulls(size)
        }
    }

}