package com.pacogarcia.proyectopeluqueria.clasesestaticas

import java.security.MessageDigest

/**
 * Clase estática para el cifrado de las contraseñas
 */
object Hash {

    /**
     * Obtiene la cadena cifrada a partir de la original mediante función SHA-256
     * @param cadena cadena original
     * @return cadena cifrada
     */
    fun hash(cadena: String): String {
        val bytes = cadena.toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}