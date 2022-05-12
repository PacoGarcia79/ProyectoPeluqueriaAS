package com.pacogarcia.proyectopeluqueria.dialogos

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pacogarcia.proyectopeluqueria.ApiRestAdapter
import com.pacogarcia.proyectopeluqueria.MainActivity
import com.pacogarcia.proyectopeluqueria.R
import com.pacogarcia.proyectopeluqueria.clasesestaticas.FechasHorasUtilidad
import com.pacogarcia.proyectopeluqueria.modelos.Cita
import com.pacogarcia.proyectopeluqueria.modelos.Roles
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Diálogo personalizado que se muestra al añadir un producto a una cita.
 */
class DialogoAddProductoCita : DialogFragment(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {

    private lateinit var v: View
    private val model: ItemViewModel by activityViewModels()
    private lateinit var aceptar: Button
    private lateinit var cancelar: Button
    private lateinit var spinner: Spinner
    var listaCitasString: ArrayList<String> = ArrayList()
    var idCitaSeleccionada: Int = 0
    var posicionProducto: Int = 0
    var cantidadProducto: Int = 0
    var idProducto: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = MaterialAlertDialogBuilder(requireActivity())
        val inflater = requireActivity().layoutInflater
        v = inflater.inflate(R.layout.add_producto_cita_dialogo, null)
        builder.setView(v)

        aceptar = v.findViewById<View>(R.id.aceptar_boton) as Button
        cancelar = v.findViewById<View>(R.id.cancelar_boton) as Button
        spinner = v.findViewById(R.id.citasSpinner)
        spinner.onItemSelectedListener = this

        aceptar.setOnClickListener(this)
        cancelar.setOnClickListener(this)

        val args = requireArguments()
        posicionProducto = args.getInt("posicionProducto")
        cantidadProducto = args.getInt("cantidad")

        if(MainActivity.clickAddProductoCitaHolder){
            model.posicionProductoBusqueda = posicionProducto
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        val idUsuario = model.getUsuario.value?.idUsuario!!
        cargaCitas(
            FechasHorasUtilidad.formatLocalDateTimeParaMySQL(LocalDateTime.now()),
            FechasHorasUtilidad.formatLocalDateTimeParaMySQL(LocalDateTime.now().plusDays(15)),
            idUsuario
        )

        return dialog
    }

    override fun onClick(p0: View?) {
        when (p0) {
            aceptar -> {
                addProductoCita()
            }
            cancelar -> {
                this.dismiss()
            }
        }
    }

    /**
     * Carga las citas dependiendo del rol del usuario y las settea en el spinner
     *
     * @param fechaInicio fecha comienzo del periodo a cargar
     * @param fechaFin fecha fin del periodo a cargar
     * @param idUsuario id del usuario al que pertenecen las citas
     */
    fun cargaCitas(fechaInicio: String, fechaFin: String, idUsuario: Int) {
        var job: ArrayList<Cita>
        CoroutineScope(Dispatchers.Main).launch {

            job = when (model.rol) {
                Roles.CLIENTE -> {
                    ApiRestAdapter.cargarCitasCliente(fechaInicio, fechaFin, idUsuario).await()
                }
                Roles.EMPLEADO -> {
                    ApiRestAdapter.cargarCitasEmpleado(fechaInicio, fechaFin, idUsuario).await()
                }
                else -> {
                    ApiRestAdapter.cargarCitasTotales(fechaInicio, fechaFin).await()
                }
            }

            model.setCitas(job)

            setSpinner()
        }
    }

    /**
     * Settea el spinner. Si el usuario es un cliente, muestra el nombre del profesional. Si es un profesional, muestra el cliente.
     */
    private fun setSpinner() {

        val citas = model.getCitas().value!!
        var nombreMostrar: String
        for (i in citas.indices) {
            when (model.rol) {
                Roles.ADMIN -> {
                    nombreMostrar = citas[i].cliente!!
                }
                else -> {
                    nombreMostrar = citas[i].nombre!!
                }
            }
            listaCitasString.add(
                FechasHorasUtilidad.formatDateToString(citas[i].fecha!!) + " - " + citas[i].hora?.substringBeforeLast(
                    ":"
                ) + " - " + nombreMostrar
            )
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listaCitasString
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
    }

    /**
     * Método usado para añadir el producto a la cita. Diferencia si el producto se añade desde el resultado de la búsqueda
     * o desde la vista entera del producto
     */
    fun addProductoCita() {

        val dialogo = this

        if (MainActivity.clickAddProductoCitaHolder) {
            idProducto = model.getProductosPorBusqueda().value?.get(posicionProducto)?.idProducto!!
        } else {
            idProducto = model.getProductosPorGrupo().value?.get(posicionProducto)?.idProducto!!
        }


        CoroutineScope(Dispatchers.Main).launch {
            val resultado =
                ApiRestAdapter.addProductoCita(idCitaSeleccionada, idProducto, cantidadProducto)
                    .await()

            if (resultado.mensaje.equals("Registro insertado")) {

                Toast.makeText(
                    requireContext(),
                    "Producto añadido",
                    Toast.LENGTH_SHORT
                ).show()

//                val result = true
//                // Use the Kotlin extension in the fragment-ktx artifact
//                setFragmentResult("requestKey", bundleOf("bundleKey" to result))

                val result = true
                if(MainActivity.clickAddProductoCitaHolder){
                    setFragmentResult("busquedaKey", bundleOf("bundleBusqueda" to result))
                    MainActivity.clickAddProductoCitaHolder = false
                }
                else{
                    setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                }


            } else {
                Toast.makeText(
                    activity,
                    "No se ha podido añadir",
                    Toast.LENGTH_SHORT
                ).show()
            }


            dialogo.dismiss()
        }
    }

    /**
     * Obtiene el id de la cita seleccionada en el spinner
     */
    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        idCitaSeleccionada = model.getCitas().value!![p2].idCita!!
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}

