package com.pacogarcia.proyectopeluqueria.clasesrecycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.pacogarcia.proyectopeluqueria.modelos.Cita
import com.pacogarcia.proyectopeluqueria.R

/**
 *  Clase del adaptador para el recycler del listado de reservas confirmadas
 */
class AdaptadorReservas(var datos: ArrayList<Cita>, val contexto: Context) :
    RecyclerView.Adapter<HolderReservas>(), View.OnClickListener {

    lateinit var listenerClick: View.OnClickListener

    private var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HolderReservas {

        val itemView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.cita_card_layout, viewGroup, false)

        itemView.setOnClickListener(this)

        return HolderReservas(itemView, contexto)
    }

    override fun onBindViewHolder(holder: HolderReservas, position: Int) {
        val item: Cita = datos[position]
        holder.bind(item, tracker)
    }

    override fun getItemCount(): Int = datos.size

    override fun getItemId(position: Int): Long = position.toLong()

    fun onClick(listener: View.OnClickListener) {
        this.listenerClick = listener
    }

    override fun onClick(p0: View?) {
        listenerClick.onClick(p0)
    }

    fun setTracker(tracker: SelectionTracker<Long>?) {
        this.tracker = tracker
    }

    fun setData(nuevaLista: ArrayList<Cita>) {
        datos = nuevaLista
    }
}