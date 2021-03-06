package com.pacogarcia.proyectopeluqueria

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pacogarcia.proyectopeluqueria.clasesrecycler.AdaptadorListaProductos
import com.pacogarcia.proyectopeluqueria.databinding.FragmentListaProductosBusquedaBinding
import com.pacogarcia.proyectopeluqueria.dialogos.ProgressDialogo
import com.pacogarcia.proyectopeluqueria.modelos.Producto
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import kotlinx.coroutines.*
import java.net.SocketTimeoutException

/**
 * Fragmento para la gestión del listado de productos resultante de la búsqueda
 */
class FragmentListaProductosBusqueda : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentListaProductosBusquedaBinding
    private lateinit var adaptador: AdaptadorListaProductos
    private lateinit var recycler: RecyclerView
    private lateinit var navController: NavController
    private val model: ItemViewModel by activityViewModels()
    var posicion = 0

    /**
     * setFragmentResultListener se usa para pasar resultado entre fragmentos. En este caso espero el resultado del
     * DialogoAddProductoCita, en concreto si la adición del producto se ha producido. Si es así, disminuyo una cantidad del
     * stock del producto de interés y notifico al adaptador el cambio en ese producto.
     * Uso la propiedad posicionProductoBusqueda del viewModel para no perder la referencia al volver a este fragmento desde
     * el Diálogo.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("busquedaKey") { requestKey, bundle ->
            val result = bundle.getBoolean("bundleBusqueda")
            if (result) {
                val producto =
                    model.getProductosPorBusqueda().value!!.get(model.posicionProductoBusqueda)
                producto.stock = producto.stock?.minus(1)
                adaptador.notifyItemChanged(model.posicionProductoBusqueda)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentListaProductosBusquedaBinding.inflate(inflater, container, false)

        recycler = binding.recyclerListProductos

        navController = NavHostFragment.findNavController(this)


        /**
         * Controla la vuelta al fragmento mediante backpressed. Si no existen elementos en el listado de productos por búsqueda,
         * los carga desde el API.
         * En caso contrario, carga el adaptador con los datos que existen en el listado.
         */
        if (model.getProductosPorBusqueda().value!!.size == 0) {
            getProductosSearch(model.query)
        } else {
            iniciaAdaptadorRecycler(model.getProductosPorBusqueda().value!!)
        }

        /**
         * Listener para la barra de búsqueda
         */
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val listaNueva: ArrayList<Producto> = ArrayList()
                model.setProductosPorBusqueda(listaNueva)
                model.query = query

                val deferred = lifecycleScope.async(Dispatchers.IO) {
                    ApiRestAdapter.cargarProductosSearch(query).await()
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
                            model.setProductosPorBusqueda(result)

                            if (model.getProductosPorBusqueda().value!!.size == 0) {
                                Toast.makeText(
                                    requireContext(),
                                    "No hay resultados",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            updateRecyclerData(model.getProductosPorBusqueda().value!!)

                        } finally {
                            // when deferred finishes and exits try block finally
                            // will be invoked and we can cancel the progress dialog
                            dialog.dismiss()

                        }
                    } else {
                        // if deferred completed already withing the wait time, skip
                        // showing the progress dialog and post the deferred result
                        val result = deferred.await()
                        model.setProductosPorBusqueda(result)

                        if (model.getProductosPorBusqueda().value!!.size == 0) {
                            Toast.makeText(requireContext(), "No hay resultados", Toast.LENGTH_LONG)
                                .show()
                        }

                        updateRecyclerData(model.getProductosPorBusqueda().value!!)
                    }
                }

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        return binding.root
    }

    /**
     * Obtiene los productos resultado de la búsqueda por palabra
     *
     * @param query cadena para la búsqueda
     */
    fun getProductosSearch(query: String) {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            ApiRestAdapter.cargarProductosSearch(query).await()
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
                    model.setProductosPorBusqueda(result)

                    if (model.getProductosPorBusqueda().value!!.size == 0) {
                        Toast.makeText(requireContext(), "No hay resultados", Toast.LENGTH_LONG)
                            .show()
                    }

                    iniciaAdaptadorRecycler(model.getProductosPorBusqueda().value!!)

                } catch (e: SocketTimeoutException) {
                    Toast.makeText(
                        activity,
                        "Error al acceder a la base de datos",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } catch (e: IllegalStateException) {
                    Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                        .show()

                    navegarInicio()
                } finally {
                    // when deferred finishes and exits try block finally
                    // will be invoked and we can cancel the progress dialog
                    dialog.dismiss()

                }
            } else {
                // if deferred completed already withing the wait time, skip
                // showing the progress dialog and post the deferred result
                try {

                    val result = deferred.await()
                    model.setProductosPorBusqueda(result)

                    if (model.getProductosPorBusqueda().value!!.size == 0) {
                        Toast.makeText(requireContext(), "No hay resultados", Toast.LENGTH_LONG).show()
                    }

                    iniciaAdaptadorRecycler(model.getProductosPorBusqueda().value!!)

                } catch (e: SocketTimeoutException) {
                    Toast.makeText(
                        activity,
                        "Error al acceder a la base de datos",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } catch (e: IllegalStateException) {
                    Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                        .show()

                    navegarInicio()
                }
            }
        }
    }

    /**
     * Inicia el adaptador y el recycler
     *
     * @param datos array de datos para el adaptador
     */
    fun iniciaAdaptadorRecycler(datos: ArrayList<Producto>) {

        adaptador = AdaptadorListaProductos(datos, requireContext())
        recycler.setHasFixedSize(true)
        recycler.adapter = adaptador
        if (context?.resources!!
                .configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            recycler.layoutManager =
                GridLayoutManager(activity, 2)
        } else {
            recycler.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }
        adaptador.onClick(this)
    }

    /**
     * Envía mediante bundle la posición del producto en el listado resultado de la búsqueda
     * Al hacer click en el producto de la lista, navega hacía su propia página con todos los datos
     */
    override fun onClick(p0: View?) {
        posicion = recycler.getChildAdapterPosition(p0!!)
        val bundle = Bundle().apply {
            putInt("posicion", posicion)
        }

        navController.navigate(R.id.action_fragmentListaProductos_to_fragmentTipoProducto, bundle)
    }

    /**
     * Actualiza el adaptador con los nuevos datos
     *
     * @param productos nuevo array de datos para el adaptador
     */
    fun updateRecyclerData(productos: ArrayList<Producto>) {
        adaptador.setData(productos)
        adaptador.notifyDataSetChanged()
    }

    /**
     * Controla el cambio de orientación para modificar el layout del recycler
     */
    override fun onConfigurationChanged(newConfig: Configuration) {

        if (newConfig != null) {
            super.onConfigurationChanged(newConfig)
        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recycler.layoutManager =
                GridLayoutManager(activity, 2)
        } else {
            recycler.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }
    }

    fun navegarInicio() {
        val contextoFragment = this
        MainActivity.autorizado = false
        val navController = NavHostFragment.findNavController(contextoFragment)
        navController.navigate(R.id.action_global_fragmentInicio2)
    }
}