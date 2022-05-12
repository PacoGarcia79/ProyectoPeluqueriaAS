package com.pacogarcia.proyectopeluqueria.clasesrecycler

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pacogarcia.proyectopeluqueria.MainActivity
import com.pacogarcia.proyectopeluqueria.R
import com.pacogarcia.proyectopeluqueria.clasesestaticas.ImagenUtilidad
import com.pacogarcia.proyectopeluqueria.modelos.Producto

/**
 *  Clase holder para el recycler del listado de productos obtenidos en una búsqueda
 */
class HolderListaProductos(v: View, contexto: Context) : RecyclerView.ViewHolder(v),
    View.OnClickListener {

    val nombreProducto: TextView
    val precioProducto: TextView
    val descripcionProducto: TextView
    val stockProducto: TextView
    val imagenProducto: ImageView
    val contexto: Context
    val v: View
    val addCitaBtn: FloatingActionButton
    private lateinit var entity: Producto
    private var posicion = 0

    private var actividadPrincipal: MainActivity

    fun bind(entity: Producto, position: Int) {
        this.entity = entity
        this.posicion = position

        setNombreProducto()

        nombreProducto.text = entity.nombre
        descripcionProducto.text = entity.descripcion?.substringBefore(".")
        precioProducto.text = "${entity.precio.toString()}€"
        imagenProducto.setImageBitmap(ImagenUtilidad.convertirStringBitmap(entity.foto))
        stockProducto.text = "${entity.stock} unidades en stock"
    }

    private fun setNombreProducto() {
        if (contexto.resources!!
                .configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            nombreProducto.textSize = 15f
        } else {
            nombreProducto.textSize = 18f
        }
    }

    init {
        this.contexto = contexto
        this.v = v
        addCitaBtn = v.findViewById(R.id.addCitaBtn)
        imagenProducto = v.findViewById(R.id.fotoProducto)
        nombreProducto = v.findViewById(R.id.nombreProducto)
        precioProducto = v.findViewById(R.id.precioProducto)
        descripcionProducto = v.findViewById(R.id.descripcionProducto)
        stockProducto = v.findViewById(R.id.stockProducto)

        addCitaBtn.setOnClickListener(this)
        actividadPrincipal = (contexto as MainActivity)
    }


    override fun onClick(p0: View?) {
        if (p0?.id == R.id.addCitaBtn) {

            if (entity.stock == 0) {
                Snackbar.make(v, "No hay unidades en stock", Snackbar.LENGTH_LONG)
                    .show()
            } else {
                val bundle = Bundle().apply {
                    putInt("posicionProducto", posicion)
                    putInt("cantidad", cantidadProductoAnyadirHolder)
                }

                MainActivity.clickAddProductoCitaHolder = true
                actividadPrincipal.navController.navigate(
                    R.id.action_global_dialogoAddProductoCita,
                    bundle
                )

                //stockProducto.text = "${entity.stock!! - 1} unidades en stock"
            }
        }
    }

    companion object {
        const val cantidadProductoAnyadirHolder = 1
    }
}