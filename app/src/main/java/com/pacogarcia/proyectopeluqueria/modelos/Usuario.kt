package com.pacogarcia.proyectopeluqueria.modelos

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Entidad usada para la gestión de los usuarios que harán uso de la aplicación.
 */
class Usuario() : Parcelable {
    var idUsuario: Int?
    var nombre: String?
    var apellidos: String?
    var username: String?
    var password: String?
    var fechaAlta: Date?
    var fechaBaja: Date?
    var rol: Roles?
    var foto: String?
    var email: String?
    var telefono: String?

    constructor(parcel: Parcel) : this() {
        idUsuario = parcel.readValue(Int::class.java.classLoader) as? Int
        nombre = parcel.readString()
        apellidos = parcel.readString()
        username = parcel.readString()
        password = parcel.readString()
        fechaAlta = parcel.readSerializable() as Date?
        fechaBaja = parcel.readSerializable() as Date?
        rol = parcel.readSerializable() as Roles?
        foto = parcel.readString()
        email = parcel.readString()
        telefono = parcel.readString()
    }


    init {
        idUsuario = 0
        nombre = ""
        apellidos = ""
        username = ""
        password = ""
        fechaAlta = null
        fechaBaja = null
        rol = null
        foto = ""
        email = ""
        telefono = ""
    }


    constructor(
        idUsuario: Int,
        nombre: String,
        apellidos: String,
        username: String,
        password: String,
        fechaAlta: Date,
        fechaBaja: Date,
        rol: Roles,
        foto: String
    ) : this() {
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.apellidos = apellidos
        this.username = username
        this.password = password
        this.fechaAlta = fechaAlta
        this.fechaBaja = fechaBaja
        this.rol = rol
        this.foto = foto
    }

    constructor(
        idUsuario: Int,
        nombre: String,
        apellidos: String,
        username: String,
        password: String,
        fechaAlta: Date,
        fechaBaja: Date,
        foto: String
    ) : this() {
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.apellidos = apellidos
        this.username = username
        this.password = password
        this.fechaAlta = fechaAlta
        this.fechaBaja = fechaBaja
        this.foto = foto
    }

    constructor(
        idUsuario: Int,
        nombre: String,
        apellidos: String,
        username: String,
        email: String,
        telefono: String
    ) : this() {
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.apellidos = apellidos
        this.username = username
        this.email = email
        this.telefono = telefono
    }

    constructor(
        idUsuario: Int,
        nombre: String,
        apellidos: String,
        username: String,
        password: String,
        email: String,
        telefono: String
    ) : this() {
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.apellidos = apellidos
        this.username = username
        this.password = password
        this.email = email
        this.telefono = telefono
    }


    constructor(
        idUsuario: Int,
        nombre: String,
        apellidos: String,
        username: String,
        rol: Roles,
        foto: String,
        email: String,
        telefono: String
    ) : this() {
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.apellidos = apellidos
        this.username = username
        this.rol = rol
        this.foto = foto
        this.email = email
        this.telefono = telefono
    }

    constructor(
        nombre: String,
        apellidos: String,
        username: String,
        password: String,
        fechaAlta: Date,
        rol: Roles,
        email: String,
        telefono: String
    ) : this() {
        this.nombre = nombre
        this.apellidos = apellidos
        this.username = username
        this.password = password
        this.fechaAlta = fechaAlta
        this.rol = rol
        this.email = email
        this.telefono = telefono
    }

//    constructor(
//        nombre: String,
//        apellidos: String,
//        username: String,
//        password: String,
//        email: String,
//        telefono: String
//    ) : this() {
//        this.nombre = nombre
//        this.apellidos = apellidos
//        this.username = username
//        this.password = password
//        this.email = email
//        this.telefono = telefono
//    }

    constructor(
        nombre: String,
        apellidos: String,
        username: String,
        password: String,
        email: String,
        telefono: String,
        foto: String,
    ) : this() {
        this.nombre = nombre
        this.apellidos = apellidos
        this.username = username
        this.password = password
        this.email = email
        this.telefono = telefono
        this.foto = foto
    }

    constructor(
        idUsuario: Int,
        nombre: String,
        apellidos: String,
        username: String
    ) : this() {
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.apellidos = apellidos
        this.username = username
    }

    constructor(
        username: String,
        password: String
    ) : this() {
        this.username = username
        this.password = password
    }

    constructor(
        idUsuario: Int,
        nombre: String
    ) : this() {
        this.idUsuario = idUsuario
        this.nombre = nombre
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(idUsuario)
        parcel.writeString(nombre)
        parcel.writeString(apellidos)
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeSerializable(fechaAlta)
        parcel.writeSerializable(fechaBaja)
        parcel.writeSerializable(rol)
        parcel.writeString(foto)
        parcel.writeString(email)
        parcel.writeString(telefono)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Usuario(idUsuario=$idUsuario, nombre=$nombre, apellidos=$apellidos, username=$username, password=$password, fechaAlta=$fechaAlta, fechaBaja=$fechaBaja, rol=$rol, foto=$foto, email=$email, telefono=$telefono)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Usuario

        if (idUsuario != other.idUsuario) return false
        if (nombre != other.nombre) return false
        if (apellidos != other.apellidos) return false
        if (username != other.username) return false
        if (password != other.password) return false
        if (fechaAlta != other.fechaAlta) return false
        if (fechaBaja != other.fechaBaja) return false
        if (rol != other.rol) return false
        if (foto != other.foto) return false
        if (email != other.email) return false
        if (telefono != other.telefono) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idUsuario ?: 0
        result = 31 * result + (nombre?.hashCode() ?: 0)
        result = 31 * result + (apellidos?.hashCode() ?: 0)
        result = 31 * result + (username?.hashCode() ?: 0)
        result = 31 * result + (password?.hashCode() ?: 0)
        result = 31 * result + (fechaAlta?.hashCode() ?: 0)
        result = 31 * result + (fechaBaja?.hashCode() ?: 0)
        result = 31 * result + (rol?.hashCode() ?: 0)
        result = 31 * result + (foto?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (telefono?.hashCode() ?: 0)
        return result
    }

    companion object CREATOR : Parcelable.Creator<Usuario> {
        override fun createFromParcel(parcel: Parcel): Usuario {
            return Usuario(parcel)
        }

        override fun newArray(size: Int): Array<Usuario?> {
            return arrayOfNulls(size)
        }
    }
}

