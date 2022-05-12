package com.pacogarcia.proyectopeluqueria.clasesrecycler

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

/**
 * ItemDetailsLookup permite que la biblioteca de selección acceda a información sobre los elementos de RecyclerView dado un MotionEvent.
 * Crea instancias de ItemDetails que están respaldadas por una instancia de RecyclerView.ViewHolder.
 */
class MyItemDetailsLookupReservas(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as HolderReservas)
                .getItemDetails()
        }
        return null
    }
}