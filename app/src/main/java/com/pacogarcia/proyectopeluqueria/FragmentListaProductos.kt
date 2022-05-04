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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pacogarcia.proyectopeluqueria.clasesrecycler.AdaptadorListaProductos
import com.pacogarcia.proyectopeluqueria.databinding.FragmentListaProductosBinding
import com.pacogarcia.proyectopeluqueria.modelos.Producto
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentListaProductos : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentListaProductosBinding
    private lateinit var adaptador: AdaptadorListaProductos
    private lateinit var recycler: RecyclerView
    private lateinit var navController: NavController
    private val model: ItemViewModel by activityViewModels()
    var posicion = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentListaProductosBinding.inflate(inflater, container, false)

        recycler = binding.recyclerListProductos

        navController = NavHostFragment.findNavController(this)

        if(model.getProductosPorBusqueda().value!!.size == 0){
            getProductosSearch(model.query)
        }
        else{
            iniciaAdaptadorRecycler(model.getProductosPorBusqueda().value!!)
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val listaNueva : ArrayList<Producto> = ArrayList()
                model.setProductosPorBusqueda(listaNueva)
                model.query = query
                CoroutineScope(Dispatchers.Main).launch {
                    val job = ApiRestAdapter.cargarProductosSearch(query).await()
                    model.setProductosPorBusqueda(job)

                    if(model.getProductosPorBusqueda().value!!.size == 0){
                        Toast.makeText(requireContext(), "No hay resultados", Toast.LENGTH_LONG).show()
                    }

                    updateRecyclerData(model.getProductosPorBusqueda().value!!)
                }

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        return binding.root
    }

    fun getProductosSearch(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val job = ApiRestAdapter.cargarProductosSearch(query).await()
            model.setProductosPorBusqueda(job)

            if(model.getProductosPorBusqueda().value!!.size == 0){
                Toast.makeText(requireContext(), "No hay resultados", Toast.LENGTH_LONG).show()
            }

            iniciaAdaptadorRecycler(model.getProductosPorBusqueda().value!!)
        }
    }

    fun iniciaAdaptadorRecycler(datos: ArrayList<Producto>) {

        adaptador = AdaptadorListaProductos(datos, requireContext())
        recycler.setHasFixedSize(true)
        recycler.adapter = adaptador
        if (context?.resources!!
                .configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recycler.layoutManager =
                GridLayoutManager(activity, 2)
        } else {
            recycler.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }
        adaptador.onClick(this)
    }

    override fun onClick(p0: View?) {
        posicion = recycler.getChildAdapterPosition(p0!!)
        val bundle = Bundle().apply {
            putInt("posicion", posicion)
        }

        navController.navigate(R.id.action_fragmentListaProductos_to_fragmentTipoProducto, bundle)
    }

    fun updateRecyclerData(productos: ArrayList<Producto>) {
        adaptador.setData(productos)
        adaptador.notifyDataSetChanged()
    }

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


}