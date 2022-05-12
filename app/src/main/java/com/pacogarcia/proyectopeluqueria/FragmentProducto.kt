package com.pacogarcia.proyectopeluqueria

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pacogarcia.proyectopeluqueria.clasesestaticas.ImagenUtilidad
import com.pacogarcia.proyectopeluqueria.dialogos.ProgressDialogo
import com.pacogarcia.proyectopeluqueria.modelos.Producto
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import kotlinx.coroutines.*

/**
 * Fragmento para la gestión de cada producto
 */
class FragmentProducto : Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {

    var posicion = 0
    var swipeDetector: SwipeDetector = SwipeDetector()
    private lateinit var navController: NavController

    private val model: ItemViewModel by activityViewModels()

    private lateinit var viewLayout : View

    private var producto: Producto? = Producto()
    private var productoBusqueda: Producto? = Producto()

    var cantidadSeleccionada = 1

    /**
     * setFragmentResultListener se usa para pasar resultado entre fragmentos. En este caso espero el resultado del
     * DialogoAddProductoCita, en concreto si la adición del producto se ha producido. Si es así, disminuyo la cantidad añadida del
     * del producto a su stock y setteo de nuevo el spinner con la nueva cantidad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("requestKey") { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getBoolean("bundleKey")
            // Do something with the result
            if(result){
                setSpinner(producto?.stock!! - cantidadSeleccionada)
            }
        }
    }

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

        /**
         * Gestiona la carga. Si recibe como argumento el nombre del grupo, carga los productos.
         * En caso contrario, muestra el producto desde el listado resultado de la búsqueda, rescatándolo por la posición
         */
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

    /**
     * Respecto el swipedetector, controla si viene desde la página de ListaProductos comprobando si 'model.query' tiene datos.
     * Si es así, no incorpora el deslizamiento
     */
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

    /**
     * Obtiene el producto desde la lista de productos por grupo, y lo establece en el layout
     */
    fun seleccionaProducto() {
        producto = model.getProductosPorGrupo().value?.get(posicion)
        setProducto(producto)
    }

    /**
     * Obtiene el producto desde la lista resultante de la búsqueda, y lo establece en el layout
     *
     * @param posicion posición del producto en la lista
     */
    fun seleccionaProductoPorBusqueda(posicion: Int) {
        productoBusqueda = model.getProductosPorBusqueda().value?.get(posicion)
        setProducto(productoBusqueda)
    }

    /**
     * Establece los datos del producto en la vista, incluyendo el spinner con las cantidades del stock
     *
     * @param producto producto a settear
     */
    private fun setProducto(producto: Producto?) {

        viewLayout.findViewById<TextView>(R.id.nombreProducto).text = producto?.nombre
        viewLayout.findViewById<TextView>(R.id.descripcionProducto).text = producto?.descripcion
        viewLayout.findViewById<TextView>(R.id.precioProducto).text = "${producto?.precio.toString()}€"

        viewLayout.findViewById<ImageView>(R.id.fotoProducto).setImageBitmap(ImagenUtilidad.convertirStringBitmap(producto?.foto))

        setSpinner(producto?.stock)
    }

    /**
     * Carga todos los productos por grupo y después establece el primero en la vista
     *
     * @param grupo nombre del grupo de productos
     */
//    fun cargarProductoPorGrupo(grupo: String) {
//        CoroutineScope(Dispatchers.Main).launch {
//            val job = ApiRestAdapter.cargarProductoPorGrupo(grupo).await()
//            model.setProductosPorGrupo(job)
//
//            seleccionaProducto()
//        }
//    }

    fun cargarProductoPorGrupo(grupo: String) {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            ApiRestAdapter.cargarProductoPorGrupo(grupo).await()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            // delay showing the progress dialog for whatever time you want
            delay(300)

            // check if the task is still active
            if (deferred.isActive) {

                // show loading dialog to user if the task is taking time
                val dialog = ProgressDialogo.progressDialog(requireContext())

                try {
                    dialog.show()

                    // suspend the coroutine till deferred finishes its task
                    // on completion, deferred result will be posted to the
                    // function and try block will be exited.
                    val result = deferred.await()
                    model.setProductosPorGrupo(result)

                    seleccionaProducto()

                } finally {
                    // when deferred finishes and exits try block finally
                    // will be invoked and we can cancel the progress dialog
                    dialog.dismiss()

                }
            } else {
                // if deferred completed already withing the wait time, skip
                // showing the progress dialog and post the deferred result
                val result = deferred.await()
                model.setProductosPorGrupo(result)

                seleccionaProducto()
            }
        }
    }

    /**
     * Establece el spinner con los datos del stock de cada producto
     *
     * @param producto producto del que se obtiene el stock
     */
    private fun setSpinner(cantidadStock: Int?) {

        val spinner = viewLayout.findViewById<Spinner>(R.id.cantidadSpinner)
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


    /**
     * Llena un array numérico con el stock, desde 1 hasta el máximo de stock disponible
     *
     * @param cantidad stock en forma de entero
     * @return array con el stock
     */
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
