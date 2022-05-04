package com.pacogarcia.proyectopeluqueria.clasesestaticas

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Clase estática con métodos para el tratamiento de fechas
 */
object FechasHorasUtilidad {

    /**
     * Método para formatear desde LocalDateTime a string, para su uso en MySQL.
     *
     * @param fecha Fecha en formato LocalDateTime.
     * @return Fecha en formato string.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTimeParaMySQL(fecha: LocalDateTime) : String{
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
        return fecha.format(formatter)
    }

    /**
     * Método para formatear desde LocalDateTime a string.
     *
     * @param fecha Fecha en formato LocalDateTime.
     * @return Fecha en formato string.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(fecha: LocalDateTime) : String{
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return fecha.format(formatter)
    }

    /**
     * Método para formatear desde Date a string.
     *
     * @param fecha Fecha en formato Date.
     * @return Fecha en formato string.
     */
    fun formatDateToString(fecha: Date) : String{
        val pattern = "dd-MM-yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(fecha)
    }

    /**
     * Método para formatear desde Date a string para MySQL.
     *
     * @param fecha Fecha en formato Date.
     * @return Fecha en formato string.
     */
    fun formatDateToStringParaMySQL(fecha: Date) : String{
        val pattern = "yyyy-MM-dd"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(fecha)
    }

    /**
     * Método para formatear desde Long a string para MySQL.
     *
     * @param fecha Fecha en formato Long.
     * @return Fecha en formato string.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertirFechaMillAString(fecha: Long): String {
        return Instant.ofEpochMilli(fecha)
            .atZone(ZoneId.of("Europe/Berlin"))
            .toLocalDate()
            .format(
                DateTimeFormatter.ofPattern("uuuu-MM-dd")
            )
    }

    /**
     * Método para formatear desde Long a string.
     *
     * @param fecha Fecha en formato Long.
     * @return Fecha en formato string.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertirFechaMillAStringMostrar(fecha: Long): String {
        return Instant.ofEpochMilli(fecha)
            .atZone(ZoneId.of("Europe/Berlin"))
            .toLocalDate()
            .format(
                DateTimeFormatter.ofPattern("dd-MM-uuuu")
            )
    }




}