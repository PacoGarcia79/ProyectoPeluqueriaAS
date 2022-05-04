package com.pacogarcia.proyectopeluqueria.clasesrecycler

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pacogarcia.proyectopeluqueria.modelos.ProductoGrupo
import com.pacogarcia.proyectopeluqueria.R
import com.pacogarcia.proyectopeluqueria.clasesestaticas.ImagenUtilidad
import com.squareup.picasso.Picasso

/**
 *  Clase holder para el recycler del listado de grupos de productos
 */
class HolderProductoGrupos (v: View, contexto: Context) : RecyclerView.ViewHolder(v) {
    val imagenGrupo: ImageView
    val textNombreGrupo: TextView
    val contexto: Context = contexto
    val v:View = v
    private lateinit var entity: ProductoGrupo

    fun bind(entity: ProductoGrupo) {
        this.entity = entity
        textNombreGrupo.text = entity.nombreGrupo
        imagenGrupo.setImageBitmap(ImagenUtilidad.convertirStringBitmap(entity.foto))
    }

    init {
        imagenGrupo = v.findViewById(R.id.imagenGrupo)
        textNombreGrupo = v.findViewById(R.id.textNombreGrupo)
    }
}