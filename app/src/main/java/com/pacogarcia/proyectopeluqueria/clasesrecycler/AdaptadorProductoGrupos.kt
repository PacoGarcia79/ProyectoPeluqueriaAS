package com.pacogarcia.proyectopeluqueria.clasesrecycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pacogarcia.proyectopeluqueria.modelos.ProductoGrupo
import com.pacogarcia.proyectopeluqueria.R

/**
 *  Clase del adaptador para el recycler del listado de grupos de productos
 */
class AdaptadorProductoGrupos(var datos: ArrayList<ProductoGrupo>, val contexto: Context) :
    RecyclerView.Adapter<HolderProductoGrupos>(), View.OnClickListener {

    lateinit var listenerClick: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductoGrupos {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.producto_grupo_card_layout, parent, false)

        itemView.setOnClickListener(this)

        return HolderProductoGrupos(itemView, contexto)
    }

    override fun onBindViewHolder(holder: HolderProductoGrupos, position: Int) {
        val item: ProductoGrupo = datos[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = datos.size

    fun onClick(listener: View.OnClickListener) {
        this.listenerClick = listener
    }

    override fun onClick(p0: View?) {
        listenerClick.onClick(p0)
    }

}