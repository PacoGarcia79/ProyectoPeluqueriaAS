package com.pacogarcia.proyectopeluqueria.modelos

/**
 * Entidad usada para la gestión de la respuesta de la APIRest en lo referente al login del usuario.
 */
class MensajeLogin() {
    var id_session: String?
    var idUsuario: Int?
    var usuario: String?
    var nombre: String?
    var rol: String?
    var mensaje: String?

    init {
        id_session = ""
        idUsuario = 0
        usuario = ""
        nombre = ""
        rol = ""
        mensaje = ""
    }

    constructor(
        id_session: String,
        idUsuario: Int,
        usuario: String,
        nombre: String,
        rol: String,
        mensaje: String
    ) : this() {
        this.id_session = id_session
        this.idUsuario = idUsuario
        this.usuario = usuario
        this.nombre = nombre
        this.rol = rol
        this.mensaje = mensaje
    }

    override fun toString(): String {
        return "MensajeLogin(id_session=$id_session, idUsuario=$idUsuario, usuario=$usuario, nombre=$nombre, rol=$rol, mensaje=$mensaje)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MensajeLogin

        if (id_session != other.id_session) return false
        if (idUsuario != other.idUsuario) return false
        if (usuario != other.usuario) return false
        if (nombre != other.nombre) return false
        if (rol != other.rol) return false
        if (mensaje != other.mensaje) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id_session?.hashCode() ?: 0
        result = 31 * result + (idUsuario ?: 0)
        result = 31 * result + (usuario?.hashCode() ?: 0)
        result = 31 * result + (nombre?.hashCode() ?: 0)
        result = 31 * result + (rol?.hashCode() ?: 0)
        result = 31 * result + (mensaje?.hashCode() ?: 0)
        return result
    }


}