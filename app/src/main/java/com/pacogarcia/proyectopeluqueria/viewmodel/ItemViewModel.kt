package com.pacogarcia.proyectopeluqueria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pacogarcia.proyectopeluqueria.modelos.Cita
import com.pacogarcia.proyectopeluqueria.modelos.Producto
import com.pacogarcia.proyectopeluqueria.modelos.Roles
import com.pacogarcia.proyectopeluqueria.modelos.Usuario

/**
 * Clase que se encarga de administrar los datos para una Actividad o un Fragmento.
 * También maneja la comunicación de la Actividad/Fragmento con el resto de la aplicación.
 */
class ItemViewModel : ViewModel() {
    private val liveData = MutableLiveData<Usuario>()
    private val liveDatas = MutableLiveData<ArrayList<Usuario>>()
    private val liveDataProducto = MutableLiveData<Producto>()
    private val liveDatasProductos = MutableLiveData<ArrayList<Producto>>()
    private val liveDatasProductosBusqueda = MutableLiveData<ArrayList<Producto>>()
    private val liveDataCita = MutableLiveData<Cita>()
    private val liveDatasCitas = MutableLiveData<ArrayList<Cita>>()
    private val liveDataCliente = MutableLiveData<Usuario>()
    private val liveDatasClientes = MutableLiveData<ArrayList<Usuario>>()

    var query: String = ""
    var rol: Roles? = null
    var posicionProductoBusqueda = 0

    fun setUsuario(item: Usuario) {
        liveData.value = item
    }

    val getUsuario: LiveData<Usuario> get() = liveData

    fun setProductosPorGrupo(values: ArrayList<Producto>) {
        liveDatasProductos.value = values
    }

    fun getProductosPorGrupo(): LiveData<ArrayList<Producto>> {
        return liveDatasProductos
    }

    fun setProductosPorBusqueda(values: ArrayList<Producto>) {
        liveDatasProductosBusqueda.value = values
    }

    fun getProductosPorBusqueda(): LiveData<ArrayList<Producto>> {
        return liveDatasProductosBusqueda
    }

    fun setCitas(values: ArrayList<Cita>) {
        liveDatasCitas.value = values
    }

    fun getCitas(): LiveData<ArrayList<Cita>> {
        return liveDatasCitas
    }

    fun setClientes(values: ArrayList<Usuario>) {
        liveDatasClientes.value = values
    }

    fun getClientes(): LiveData<ArrayList<Usuario>> {
        return liveDatasClientes
    }

}