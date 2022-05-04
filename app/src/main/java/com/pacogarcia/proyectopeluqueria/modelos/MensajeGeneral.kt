package com.pacogarcia.proyectopeluqueria.modelos

/**
 * Entidad usada para la gestión de la respuesta de la APIRest.
 */
class MensajeGeneral() {

    var mensaje: String?

    init {
        mensaje = ""
    }

    constructor(
        mensaje: String
    ) : this() {
        this.mensaje = mensaje
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MensajeGeneral

        if (mensaje != other.mensaje) return false

        return true
    }

    override fun hashCode(): Int {
        return mensaje?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "MensajeGeneral(mensaje=$mensaje)"
    }


}