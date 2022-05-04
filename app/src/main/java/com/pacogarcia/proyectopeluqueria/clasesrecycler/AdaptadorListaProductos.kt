package com.pacogarcia.proyectopeluqueria.clasesrecycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pacogarcia.proyectopeluqueria.R
import com.pacogarcia.proyectopeluqueria.modelos.Producto

/**
 *  Clase del adaptador para el recycler del listado de productos obtenidos en una búsqueda
 */
class AdaptadorListaProductos (var datos: ArrayList<Producto>, val contexto: Context) :
    RecyclerView.Adapter<HolderListaProductos>(), View.OnClickListener {

    lateinit var listenerClick: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderListaProductos {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.producto_card_layout, parent, false)

        itemView.setOnClickListener(this)

        return HolderListaProductos(itemView, contexto)
    }

    override fun onBindViewHolder(holder: HolderListaProductos, position: Int) {
        val item: Producto = datos[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = datos.size

    fun onClick(listener: View.OnClickListener) {
        this.listenerClick = listener
    }

    override fun onClick(p0: View?) {
        listenerClick.onClick(p0)
    }

    fun setData(nuevaLista: ArrayList<Producto>) {
        datos = nuevaLista
    }

}