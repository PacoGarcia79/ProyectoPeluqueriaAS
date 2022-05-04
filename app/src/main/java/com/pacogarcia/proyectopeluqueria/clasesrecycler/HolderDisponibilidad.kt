package com.pacogarcia.proyectopeluqueria.clasesrecycler

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.pacogarcia.proyectopeluqueria.MainActivity
import com.pacogarcia.proyectopeluqueria.R
import com.pacogarcia.proyectopeluqueria.clasesestaticas.FechasHorasUtilidad
import com.pacogarcia.proyectopeluqueria.modelos.Cita
import com.pacogarcia.proyectopeluqueria.modelos.Disponibilidad
import com.pacogarcia.proyectopeluqueria.modelos.Roles

/**
 *  Clase holder para el recycler del listado de horarios disponibles
 */
class HolderDisponibilidad(v: View, contexto: Context) : RecyclerView.ViewHolder(v) {
    val textNombre: TextView
    val chipHora: Chip
    val chipFechas: Chip
    val contexto: Context
    val v: View
    private lateinit var entity: Disponibilidad

    fun bind(entity: Disponibilidad, tracker: SelectionTracker<Long>?) {
        this.entity = entity
        textNombre.text = entity.nombre
        chipFechas.text = "${FechasHorasUtilidad.formatDateToString(entity.fecha_comienzo!!)} - ${
            FechasHorasUtilidad.formatDateToString(entity.fecha_fin!!)
        }"
        chipHora.text = (entity.hora)!!.substringBeforeLast(":")

        if (tracker!!.isSelected(adapterPosition.toLong()))
            v.findViewById<MaterialCardView>(R.id.cardView).background =
                ColorDrawable(Color.parseColor("#F8B9B9B9"))
        else v.findViewById<MaterialCardView>(R.id.cardView).background =
            ColorDrawable(Color.parseColor("#FFFFFFFF"))
    }

    init {
        this.contexto = contexto
        this.v = v
        textNombre = v.findViewById(R.id.textNombreAgenda)
        chipHora = v.findViewById(R.id.chipHora)
        chipFechas = v.findViewById(R.id.chipFechas)
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): Long? = itemId
        }


}