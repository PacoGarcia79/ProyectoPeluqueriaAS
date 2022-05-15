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
import com.pacogarcia.proyectopeluqueria.clasesestaticas.FechasHorasUtilidad
import com.pacogarcia.proyectopeluqueria.MainActivity
import com.pacogarcia.proyectopeluqueria.modelos.Cita
import com.pacogarcia.proyectopeluqueria.modelos.Roles
import com.pacogarcia.proyectopeluqueria.R

/**
 *  Clase holder para el recycler del listado de reservas confirmadas
 */
class HolderReservas(v: View, contexto: Context) : RecyclerView.ViewHolder(v),
    View.OnClickListener {
    val textNombre: TextView
    val textFecha: TextView
    val chipServicios: Chip
    val chipProductos: Chip
    val chipHora: Chip
    val chipPrecio: Chip
    val imagen: ImageView
    val contexto: Context
    val v: View
    private lateinit var entity: Cita

    fun bind(entity: Cita, tracker: SelectionTracker<Long>?) {
        this.entity = entity
        setNombresText(entity)
        textFecha.text = FechasHorasUtilidad.formatDateToString(entity.fecha!!)
        chipServicios.text = entity.servicios
        setProductosChip(entity)
        chipHora.text = (entity.hora)!!.substringBeforeLast(":")
        chipPrecio.text = stringSumaServiciosProductos(entity)

        if (tracker!!.isSelected(adapterPosition.toLong()))
            v.findViewById<MaterialCardView>(R.id.cardView).background =
                ColorDrawable(Color.parseColor("#F8B9B9B9"))
        else v.findViewById<MaterialCardView>(R.id.cardView).background =
            ColorDrawable(Color.parseColor("#FFFFFFFF"))
    }

    private fun setProductosChip(entity: Cita) {
        if (!entity.productos.isNullOrEmpty()) {
            chipProductos.text = entity.productos
            chipProductos.visibility = View.VISIBLE
        }
    }

    private fun setNombresText(entity: Cita) {

        if (MainActivity.rol == Roles.CLIENTE) {
            textNombre.text = "Cita con ${entity.profesional}"
        } else {
            textNombre.text = "Cita con ${entity.cliente}"
        }
    }

    private fun stringSumaServiciosProductos(entity: Cita): String {
        val precioServicios: Double = entity.precio_servicios!!
        val precioProductos: Double = entity.precio_productos!!
        return (precioServicios + precioProductos).toString() + "€"
    }

    init {
        this.contexto = contexto
        this.v = v
        textNombre = v.findViewById(R.id.textNombreAgenda)
        textFecha = v.findViewById(R.id.textFechaAgenda)
        chipServicios = v.findViewById(R.id.chipServicio)
        chipProductos = v.findViewById(R.id.chipProductos)
        chipHora = v.findViewById(R.id.chipHora)
        chipPrecio = v.findViewById(R.id.chipPrecioTotal)
        imagen = v.findViewById(R.id.imagen)
        imagen.setOnClickListener(this)
        if (MainActivity.rol == Roles.CLIENTE) {
            imagen.visibility = View.INVISIBLE
        }
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey(): Long? = itemId
        }

    override fun onClick(p0: View?) {
        if (p0?.id == R.id.imagen) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + entity.telefono.toString())
            ContextCompat.startActivity(contexto, intent, null)
        }
    }
}