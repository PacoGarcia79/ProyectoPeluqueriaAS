package com.pacogarcia.proyectopeluqueria.modelos

import android.os.Parcel
import android.os.Parcelable

/**
 * Entidad usada para la gestión de los productos.
 */
class Producto() : Parcelable {
    var idProducto: Int?
    var nombre: String?
    var precio: Double?
    var idProductoGrupo: Int?
    var nombreGrupo: String?
    var descripcion: String?
    var foto: String?
    var stock: Int?


    init {
        idProducto = 0
        nombre = ""
        precio= 0.0
        idProductoGrupo = 0
        nombreGrupo = ""
        descripcion = ""
        foto = ""
        stock = 0
    }

    constructor(parcel: Parcel) : this() {
        idProducto = parcel.readValue(Int::class.java.classLoader) as? Int
        nombre = parcel.readString()
        precio = parcel.readValue(Double::class.java.classLoader) as? Double
        idProductoGrupo = parcel.readValue(Int::class.java.classLoader) as? Int
        nombreGrupo = parcel.readString()
        descripcion = parcel.readString()
        foto = parcel.readString()
        stock = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    constructor(
        nombre: String,
        precio: Double,
        idProductoGrupo: Int,
        descripcion: String,
        foto: String
    ) : this() {
        this.nombre = nombre
        this.precio = precio
        this.idProductoGrupo = idProductoGrupo
        this.descripcion = descripcion
        this.foto = foto
    }

    constructor(
        idProducto: Int,
        nombre: String,
        precio: Double,
        idProductoGrupo: Int,
        descripcion: String,
        foto: String
    ) : this() {
        this.idProducto = idProducto
        this.nombre = nombre
        this.precio = precio
        this.idProductoGrupo = idProductoGrupo
        this.descripcion = descripcion
        this.foto = foto
    }

    constructor(
        idProducto: Int,
        nombre: String,
        precio: Double,
        idProductoGrupo: Int,
        nombreGrupo: String,
        descripcion: String,
        foto: String,
        stock : Int
    ) : this() {
        this.idProducto = idProducto
        this.nombre = nombre
        this.precio = precio
        this.idProductoGrupo = idProductoGrupo
        this.nombreGrupo = nombreGrupo
        this.descripcion = descripcion
        this.foto = foto
        this.stock = stock
    }

    constructor(
        idProducto: Int,
        nombre: String,
        precio: Double,
        nombreGrupo: String,
        descripcion: String,
        foto: String
    ) : this() {
        this.idProducto = idProducto
        this.nombre = nombre
        this.precio = precio
        this.nombreGrupo = nombreGrupo
        this.descripcion = descripcion
        this.foto = foto
    }

    constructor(
        idProducto: Int,
        nombre: String,
        precio: Double,
        descripcion: String,
        foto: String
    ) : this() {
        this.idProducto = idProducto
        this.nombre = nombre
        this.precio = precio
        this.descripcion = descripcion
        this.foto = foto
    }

    init {
        this.idProducto = 0
        this.nombre = ""
        this.precio = 0.0
        this.idProductoGrupo = 0
        this.nombreGrupo = ""
        this.descripcion = ""
        this.foto = ""
        this.stock = 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(idProducto)
        parcel.writeString(nombre)
        parcel.writeValue(precio)
        parcel.writeValue(idProductoGrupo)
        parcel.writeString(nombreGrupo)
        parcel.writeString(descripcion)
        parcel.writeString(foto)
        parcel.writeValue(stock)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Producto

        if (idProducto != other.idProducto) return false
        if (nombre != other.nombre) return false
        if (precio != other.precio) return false
        if (idProductoGrupo != other.idProductoGrupo) return false
        if (nombreGrupo != other.nombreGrupo) return false
        if (descripcion != other.descripcion) return false
        if (foto != other.foto) return false
        if (stock != other.stock) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idProducto ?: 0
        result = 31 * result + (nombre?.hashCode() ?: 0)
        result = 31 * result + (precio?.hashCode() ?: 0)
        result = 31 * result + (idProductoGrupo ?: 0)
        result = 31 * result + (nombreGrupo?.hashCode() ?: 0)
        result = 31 * result + (descripcion?.hashCode() ?: 0)
        result = 31 * result + (foto?.hashCode() ?: 0)
        result = 31 * result + (stock ?: 0)
        return result
    }

    override fun toString(): String {
        return "Producto(idProducto=$idProducto, nombre=$nombre, precio=$precio, idProductoGrupo=$idProductoGrupo, nombreGrupo=$nombreGrupo, descripcion=$descripcion, foto=$foto, stock=$stock)"
    }

    companion object CREATOR : Parcelable.Creator<Producto> {
        override fun createFromParcel(parcel: Parcel): Producto {
            return Producto(parcel)
        }

        override fun newArray(size: Int): Array<Producto?> {
            return arrayOfNulls(size)
        }
    }



}