package com.pacogarcia.proyectopeluqueria

import android.app.ProgressDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pacogarcia.proyectopeluqueria.clasesrecycler.AdaptadorProductoGrupos
import com.pacogarcia.proyectopeluqueria.databinding.FragmentGrupoProductosBinding
import com.pacogarcia.proyectopeluqueria.dialogos.ProgressDialogo
import com.pacogarcia.proyectopeluqueria.modelos.Producto
import com.pacogarcia.proyectopeluqueria.modelos.ProductoGrupo
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import kotlinx.coroutines.*

/**
 * Fragmento para la gestión del listado de grupos de productos
 */
class FragmentGrupoProductos : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentGrupoProductosBinding
    private lateinit var adaptador: AdaptadorProductoGrupos
    private lateinit var recycler: RecyclerView
    private lateinit var navController: NavController
    private val model: ItemViewModel by activityViewModels()
    var posicion = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentGrupoProductosBinding.inflate(inflater, container, false)

        recycler = binding.recyclerListProductoGrupo

//        if (!MainActivity.productoGruposCargados) {
//            getProductoGrupos()
//        } else {
//            iniciaAdaptadorRecycler(MainActivity.productoGrupos!!)
//        }

        model.query = ""
        navController = NavHostFragment.findNavController(this)


        /**
         * Controla la vuelta al fragmento mediante backpressed. Si no está cargado el listado de grupos de productos,
         * los carga desde el API.
         * En caso contrario, carga el adaptador con los datos que existen en el listado.
         */
        if (!MainActivity.productoGruposCargados) {
            cargarProductoGrupos()
        } else {
            iniciaAdaptadorRecycler(MainActivity.productoGrupos!!)
        }

        /**
         * Listener para la barra de búsqueda
         */
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val listaNueva: ArrayList<Producto> = ArrayList()
                model.setProductosPorBusqueda(listaNueva)
                model.query = query
                navController.navigate(R.id.action_global_fragmentListaProductos)

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })


        return binding.root
    }


    /**
     * Obtiene los grupos de productos e inicia el recycler
     */
    fun getProductoGrupos() {
        CoroutineScope(Dispatchers.Main).launch {
            val job = ApiRestAdapter.cargarProductoGrupos().await()
            MainActivity.productoGrupos = job

            MainActivity.productoGruposCargados = true
            iniciaAdaptadorRecycler(MainActivity.productoGrupos!!)
        }
    }

    /**
     * Envía mediante bundle el nombre del grupo al FragmentProducto
     * Al hacer click en el producto de la lista, navega hacía su propia página con todos los datos
     */
    override fun onClick(p0: View?) {

        posicion = recycler.getChildAdapterPosition(p0!!)
        val nombreGrupo: String = MainActivity.productoGrupos!!.get(posicion).nombreGrupo!!
        val bundle = Bundle().apply {
            putString("nombreGrupo", nombreGrupo)
        }

        navController.navigate(R.id.action_fragmentProductos_to_fragmentTipoProducto, bundle)
    }

    /**
     * Inicia el adaptador y el recycler
     *
     * @param datos array de datos para el adaptador
     */
    fun iniciaAdaptadorRecycler(datos: ArrayList<ProductoGrupo>) {

        adaptador = AdaptadorProductoGrupos(datos, requireContext())
        recycler.setHasFixedSize(true)
        recycler.adapter = adaptador

        if (context?.resources!!
                .configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            recycler.layoutManager =
                GridLayoutManager(activity, 2)
        } else {
            recycler.layoutManager =
                GridLayoutManager(activity, 1)
        }

        adaptador.onClick(this)

    }

    /**
     * Obtiene los grupos de productos e inicia el recycler. Muestra además
     * un Progress Dialogo mientra carga los grupos
     */
    fun cargarProductoGrupos() {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            ApiRestAdapter.cargarProductoGrupos().await()
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
                    MainActivity.productoGrupos = result
                    iniciaAdaptadorRecycler(result)
                    MainActivity.productoGruposCargados = true

                } finally {
                    // when deferred finishes and exits try block finally
                    // will be invoked and we can cancel the progress dialog
                    dialog.dismiss()

                }
            } else {
                // if deferred completed already withing the wait time, skip
                // showing the progress dialog and post the deferred result
                val result = deferred.await()
                MainActivity.productoGrupos = result
                iniciaAdaptadorRecycler(result)
                MainActivity.productoGruposCargados = true
            }
        }
    }

    /**
     * Controla el cambio de orientación para modificar la cantidad de elementos que se muestran en el grid layout
     */
    override fun onConfigurationChanged(newConfig: Configuration) {

        if (newConfig != null) {
            super.onConfigurationChanged(newConfig)
        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recycler.layoutManager =
                GridLayoutManager(activity, 2)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            recycler.layoutManager =
                GridLayoutManager(activity, 1)
        }
    }
}