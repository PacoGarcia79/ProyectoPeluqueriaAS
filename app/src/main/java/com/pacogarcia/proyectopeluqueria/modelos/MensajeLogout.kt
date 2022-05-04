package com.pacogarcia.proyectopeluqueria.modelos

/**
 * Entidad usada para la gestión de la respuesta de la APIRest en lo referente al login del usuario.
 */
class MensajeLogout() {

    var id_session: String?
    var usuario: String?

    init {
        id_session = ""
        usuario = ""
    }

    constructor(
        id_session: String,
        usuario: String
    ) : this() {
        this.id_session = id_session
        this.usuario = usuario
    }
}