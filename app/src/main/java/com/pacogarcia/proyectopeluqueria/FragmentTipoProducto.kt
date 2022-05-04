package com.pacogarcia.proyectopeluqueria

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pacogarcia.proyectopeluqueria.clasesestaticas.ImagenUtilidad
import com.pacogarcia.proyectopeluqueria.databinding.FragmentTipoProductoBinding
import com.pacogarcia.proyectopeluqueria.modelos.Producto
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentTipoProducto : Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    var posicion = 0
    var swipeDetector: SwipeDetector = SwipeDetector()
    private lateinit var navController: NavController

    private val model: ItemViewModel by activityViewModels()

    private lateinit var viewLayout : View

    var cantidadSeleccionada = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        if (context?.getResources()
            ?.getConfiguration()?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewLayout = inflater.inflate(R.layout.fragment_tipo_producto_horizontal, container, false)
        }else{
            viewLayout = inflater.inflate(R.layout.fragment_tipo_producto, container, false)
        }

        navController = NavHostFragment.findNavController(this)

        val args = requireArguments()
        val nombreGrupo = args.getString("nombreGrupo")
        val posicion = args.getInt("posicion")

        if (!nombreGrupo.isNullOrEmpty()) {
            cargarProductoPorGrupo(nombreGrupo)
        } else {
            seleccionaProductoPorBusqueda(posicion)
        }

        viewLayout.findViewById<CoordinatorLayout>(R.id.vistaCompleta).setOnClickListener(this)
        swipeDetector = SwipeDetector()
        viewLayout.findViewById<CoordinatorLayout>(R.id.vistaCompleta).setOnTouchListener(swipeDetector)
        viewLayout.findViewById<FloatingActionButton>(R.id.fabHome).setOnClickListener(this)
        viewLayout.findViewById<Button>(R.id.addCitaBtn).setOnClickListener(this)

        return viewLayout
    }

    override fun onClick(p0: View?) {

        if (p0?.id == R.id.fabHome) {
            navController.navigate(R.id.action_global_fragmentProductos4)
        } else if (p0?.id == R.id.addCitaBtn) {
            val bundle = Bundle().apply {
                putInt("posicionProducto", posicion)
                putInt("cantidad", cantidadSeleccionada)
            }
            navController.navigate(
                R.id.action_global_dialogoAddProductoCita,
                bundle
            )
        } else {
            if (model.query.isEmpty()) {
                if (swipeDetector.swipeDetected()) {

                    val size: Int = model.getProductosPorGrupo().value?.size!! - 1
                    if (swipeDetector.action == SwipeDetector.Action.RL) {
                        if (posicion != size) {
                            posicion++
                        } else {
                            posicion = 0
                        }
                    } else if (swipeDetector.action == SwipeDetector.Action.LR) {
                        if (posicion != 0) {
                            posicion--
                        } else {
                            posicion = size
                        }
                    }

                    seleccionaProducto()
                }
            }
        }
    }

    fun seleccionaProducto() {
        val producto = model.getProductosPorGrupo().value?.get(posicion)
        setProducto(producto)
    }

    fun seleccionaProductoPorBusqueda(posicion: Int) {
        val producto = model.getProductosPorBusqueda().value?.get(posicion)
        setProducto(producto)
    }

    private fun setProducto(producto: Producto?) {

        viewLayout.findViewById<TextView>(R.id.nombreProducto).text = producto?.nombre
        viewLayout.findViewById<TextView>(R.id.descripcionProducto).text = producto?.descripcion
        viewLayout.findViewById<TextView>(R.id.precioProducto).text = "${producto?.precio.toString()}€"

        viewLayout.findViewById<ImageView>(R.id.fotoProducto).setImageBitmap(ImagenUtilidad.convertirStringBitmap(producto?.foto))

        setSpinner(producto)
    }

    fun cargarProductoPorGrupo(grupo: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val job = ApiRestAdapter.cargarProductoPorGrupo(grupo).await()
            model.setProductosPorGrupo(job)

            seleccionaProducto()
        }
    }

    private fun setSpinner(producto: Producto?) {

        val spinner = viewLayout.findViewById<Spinner>(R.id.cantidadSpinner)
        val cantidadStock = producto?.stock
        val listaNumeros = llenaArrayCantidadStock(cantidadStock!!)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listaNumeros
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }


    fun llenaArrayCantidadStock(cantidad : Int) : Array<Int?>{
        val lista = ArrayList<Int>()
        for(i in 1..cantidad){
            lista.add(i)
        }

        val array = arrayOfNulls<Int>(lista.size)
        lista.toArray(array)

        return array
    }

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        cantidadSeleccionada = p2 + 1
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val inflater = LayoutInflater.from(activity)
        (view as ViewGroup).removeAllViewsInLayout()

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewLayout = inflater.inflate(R.layout.fragment_tipo_producto_horizontal, view as ViewGroup)

        } else {
            viewLayout = inflater.inflate(R.layout.fragment_tipo_producto, view as ViewGroup)
        }

        viewLayout.findViewById<FloatingActionButton>(R.id.fabHome).setOnClickListener(this)
        viewLayout.findViewById<Button>(R.id.addCitaBtn).setOnClickListener(this)
        seleccionaProducto()
    }

}

