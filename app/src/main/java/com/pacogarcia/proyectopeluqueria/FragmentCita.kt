package com.pacogarcia.proyectopeluqueria

import android.R
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.pacogarcia.proyectopeluqueria.R.*
import com.pacogarcia.proyectopeluqueria.clasesestaticas.FechasHorasUtilidad
import com.pacogarcia.proyectopeluqueria.databinding.FragmentCitaBinding
import com.pacogarcia.proyectopeluqueria.modelos.*
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.util.*

/**
 * Fragmento para la gestión de las citas
 */
class FragmentCita : Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener,
    DatePickerDialog.OnDateSetListener {

    private lateinit var binding: FragmentCitaBinding
    private lateinit var spinner: Spinner
    private lateinit var fechaSeleccionadaInput: TextInputEditText
    private val model: ItemViewModel by activityViewModels()

    var idClienteSeleccionado: Int = 0
    var idHorarioSeleccionado: Int = 0
    var idEmpleadoSeleccionado: Int = 0
    var fechaSeleccionada: String = ""
    var fechaComienzoBusquedaOcupadas = ""
    var cadenaStringServicios: String = ""
    var opcionCita: OpcionesCita = OpcionesCita.HORA

    var listaCitasString: ArrayList<String> = ArrayList()
    var listaHorariosLibres: ArrayList<Horario> = ArrayList()
    var listaEmpleadosDisponiblesOpcionHora: ArrayList<Usuario> = ArrayList()
    var listaEmpleadosDisponiblesOpcionProfesional: ArrayList<Usuario> = ArrayList()
    var listaServiciosPorEmpleado: ArrayList<Servicio> = ArrayList()
    var listaFechasOcupadas: ArrayList<Date> = ArrayList()
    var arrayCalendarFechasOcupadas: Array<Calendar> = arrayOf()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentCitaBinding.inflate(inflater, container, false)
        binding.botonReservar.setOnClickListener(this)

        spinner = binding.clientesSpinner
        spinner.onItemSelectedListener = this

        fechaSeleccionadaInput = binding.fechaSeleccionadaInput
        fechaSeleccionadaInput.setOnClickListener(this)

        fechaComienzoBusquedaOcupadas =
            FechasHorasUtilidad.formatLocalDateTimeParaMySQL(LocalDateTime.now())
        cargarFechasOcupadas()

        if (model.rol != Roles.CLIENTE) {
            cargarClientes()
        } else {
            binding.bannerCliente.visibility = View.GONE
            binding.clientesSpinner.visibility = View.GONE
        }

        /**
         * Controla los cambios en el grupo de radiobuttons
         */
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.citaHora.id -> {
                    binding.layoutCitaProfesional.visibility = View.GONE
                    binding.layoutCitaHora.visibility = View.VISIBLE
                    opcionCita = OpcionesCita.HORA
                    reseteaOpcionesModoHora()
                }
                binding.citaProfesional.id -> {
                    binding.layoutCitaProfesional.visibility = View.VISIBLE
                    binding.layoutCitaHora.visibility = View.GONE
                    opcionCita = OpcionesCita.PROFESIONAL
                    reseteaOpcionesModoProfesional()
                }
            }
        }

        /**
         * Controla los cambios en el grupo de chips con los horarios, para la opción de cita por hora
         */
        binding.chipGroupHoraOpcionHora.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)

            if (group.checkedChipIds.size == 0) {
                binding.chipGroupProfesionalesOpcionHora.clearCheck()
                binding.chipGroupProfesionalesOpcionHora.removeAllViews()
                binding.bannerProfesionalOpcionHora.visibility = View.GONE
                binding.chipGroupProfesionalesOpcionHora.visibility = View.GONE
                binding.chipGroupServiciosOpcionHora.clearCheck()
                binding.chipGroupServiciosOpcionHora.removeAllViews()
                binding.bannerServicioOpcionHora.visibility = View.GONE
                binding.chipGroupServiciosOpcionHora.visibility = View.GONE
            }

            chip?.let { chipView ->
                idHorarioSeleccionado = chip.id

                cargarEmpleadosDisponiblesOpcionHora()

            } ?: kotlin.run {
            }
        }
        /**
         * Controla los cambios en el grupo de chips con los profesionales, para la opción de cita por hora
         */
        binding.chipGroupProfesionalesOpcionHora.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)

            if (group.checkedChipIds.size == 0) {
                binding.chipGroupServiciosOpcionHora.clearCheck()
                binding.chipGroupServiciosOpcionHora.removeAllViews()
                binding.bannerServicioOpcionHora.visibility = View.GONE
                binding.chipGroupServiciosOpcionHora.visibility = View.GONE
            }

            chip?.let { chipView ->
                idEmpleadoSeleccionado = chip.id

                cargarServiciosPorEmpleado()

            } ?: kotlin.run {
            }
        }

        /**
         * Controla los cambios en el grupo de chips con los profesionales, para la opción de cita por profesional
         */
        binding.chipGroupProfesionalesOpcionProfesional.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)

            if (group.checkedChipIds.size == 0) {
                binding.chipGroupServiciosOpcionProfesional.clearCheck()
                binding.chipGroupServiciosOpcionProfesional.removeAllViews()
                binding.bannerServicioOpcionProfesional.visibility = View.GONE
                binding.chipGroupServiciosOpcionProfesional.visibility = View.GONE
                binding.chipGroupHoraOpcionProfesional.clearCheck()
                binding.chipGroupHoraOpcionProfesional.removeAllViews()
                binding.bannerHoraOpcionProfesional.visibility = View.GONE
                binding.chipGroupHoraOpcionProfesional.visibility = View.GONE
            }

            chip?.let { chipView ->
                idEmpleadoSeleccionado = chip.id

                cargarServiciosPorEmpleado()
                cargarHorariosLibresEmpleadosFecha()

            } ?: kotlin.run {
            }
        }

        /**
         * Controla los cambios en el grupo de chips con los horarios, para la opción de cita por profesional
         */
        binding.chipGroupHoraOpcionProfesional.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)

            if (group.checkedChipIds.size == 0) {
                idHorarioSeleccionado = 0
            }

            chip?.let { chipView ->
                idHorarioSeleccionado = chip.id

            } ?: kotlin.run {
            }
        }

        return binding.root
    }

    /**
     * Obtiene la cadena de string con los ids de los servicios separados por coma
     * a partir de la MutableList de ids
     */
    fun obtieneListadoServicios(lista: MutableList<Int>): String {
        val sb = StringBuilder()
        for (i in 0 until lista.size) {
            if (i < lista.size - 1) {
                sb.append(lista[i].toString()).append(",")
            } else {
                sb.append(lista[i].toString())
            }
        }
        return sb.toString()
    }


    fun checkData() {
        if (model.rol != Roles.CLIENTE) {
            if (idHorarioSeleccionado == 0 || idEmpleadoSeleccionado == 0 || idClienteSeleccionado == 0
                || cadenaStringServicios.isEmpty()
            ) {
                Toast.makeText(activity, "Seleccione todos los datos necesarios", Toast.LENGTH_LONG)
                    .show()
            } else {
                addCita()
            }
        } else {
            if (idHorarioSeleccionado == 0 || idEmpleadoSeleccionado == 0 || cadenaStringServicios.isEmpty()) {
                Toast.makeText(activity, "Seleccione todos los datos necesarios", Toast.LENGTH_LONG)
                    .show()
            } else {
                addCita()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0) {
            binding.botonReservar -> {

                val checkedChipIds: MutableList<Int>

                if (opcionCita == OpcionesCita.HORA) {
                    checkedChipIds = binding.chipGroupServiciosOpcionHora.checkedChipIds
                } else {
                    checkedChipIds = binding.chipGroupServiciosOpcionProfesional.checkedChipIds
                }

                cadenaStringServicios = obtieneListadoServicios(checkedChipIds)

                checkData()

            }
            //Selecciona las fechas no disponibles y muestra el calendario con un máximo de 60 días.
            fechaSeleccionadaInput -> {
                val now = Calendar.getInstance()
                val dpd = DatePickerDialog.newInstance(
                    this,
                    now[Calendar.YEAR],
                    now[Calendar.MONTH],
                    now[Calendar.DAY_OF_MONTH]
                )

                val cal = Calendar.getInstance()
                cal.add(Calendar.DATE, 60)

                dpd.apply {
                    autoDismiss(true)
                    setOnCancelListener(DialogInterface.OnCancelListener {
                        dpd.dismiss()
                    })
                    minDate = now
                    maxDate = cal
                    disabledDays = arrayCalendarFechasOcupadas
                }.show(parentFragmentManager, "Datepickerdialog")
            }
        }
    }

    /**
     * Obtiene el array de fechas no disponibles en formato Calendar, necesario para
     * asignarlo a disableDays del DatePickerDialog
     */
    fun llenaArrayCalendarFechasOcupadas() {
        val listaCalendarFechasOcupadas: ArrayList<Calendar> = ArrayList()
        var tCalendar: Calendar
        listaFechasOcupadas.forEach {
            tCalendar = Calendar.getInstance()
            tCalendar.time = it
            listaCalendarFechasOcupadas.add(tCalendar)
        }

        arrayCalendarFechasOcupadas = listaCalendarFechasOcupadas.toTypedArray()
    }

    /**
     * Obtiene las fechas no disponibles para reservar citas
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarFechasOcupadas() {
        var job: ArrayList<Date>
        CoroutineScope(Dispatchers.Main).launch {

            try {

                job = ApiRestAdapter.cargarFechasOcupadas(fechaComienzoBusquedaOcupadas)
                    .await()

                listaFechasOcupadas = job
                llenaArrayCalendarFechasOcupadas()

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IllegalStateException) {
                Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                    .show()

                navegarInicio()
            }

        }
    }

    /**
     * Obtiene los empleados disponibles en la opción de citas por hora
     * y rellena los chips. Si se selecciona un empleado distinto, resetea los chips de los servicios.
     */
    fun cargarEmpleadosDisponiblesOpcionHora() {
        var job: ArrayList<Usuario>
        CoroutineScope(Dispatchers.Main).launch {

            try {

                job =
                    ApiRestAdapter.cargarEmpleadosDisponiblesOpcionHora(
                        idHorarioSeleccionado,
                        fechaSeleccionada
                    )
                        .await()

                listaEmpleadosDisponiblesOpcionHora = job

                binding.bannerServicioOpcionHora.visibility = View.GONE
                binding.chipGroupServiciosOpcionHora.visibility = View.GONE
                binding.chipGroupServiciosOpcionHora.clearCheck()
                binding.chipGroupServiciosOpcionHora.removeAllViews()

                binding.bannerProfesionalOpcionHora.visibility = View.VISIBLE
                binding.chipGroupProfesionalesOpcionHora.visibility = View.VISIBLE
                binding.chipGroupProfesionalesOpcionHora.clearCheck()
                binding.chipGroupProfesionalesOpcionHora.removeAllViews()

                for (empleado in listaEmpleadosDisponiblesOpcionHora) {
                    val chip =
                        layoutInflater.inflate(
                            layout.single_chip_layout,
                            binding.chipGroupProfesionalesOpcionHora,
                            false
                        ) as Chip
                    chip.text = "${empleado.nombre} ${empleado.apellidos}"
                    chip.id = empleado.idUsuario!!
                    binding.chipGroupProfesionalesOpcionHora.addView(chip)
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IllegalStateException) {
                Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                    .show()

                navegarInicio()
            }
        }
    }

    /**
     * Obtiene los empleados disponibles en la opción de citas por profesional
     * y rellena los chips. Si se selecciona un empleado distinto, resetea los chips de los servicios.
     */
    fun cargarEmpleadosDisponiblesOpcionProfesional() {
        var job: ArrayList<Usuario>
        CoroutineScope(Dispatchers.Main).launch {

            try {

                job =
                    ApiRestAdapter.cargarEmpleadosDisponiblesOpcionProfesional(fechaSeleccionada)
                        .await()

                listaEmpleadosDisponiblesOpcionProfesional = job

                binding.bannerServicioOpcionProfesional.visibility = View.GONE
                binding.chipGroupServiciosOpcionProfesional.visibility = View.GONE
                binding.chipGroupServiciosOpcionProfesional.clearCheck()
                binding.chipGroupServiciosOpcionProfesional.removeAllViews()

                binding.bannerProfesionalOpcionProfesional.visibility = View.VISIBLE
                binding.chipGroupProfesionalesOpcionProfesional.visibility = View.VISIBLE
                binding.chipGroupProfesionalesOpcionProfesional.clearCheck()
                binding.chipGroupProfesionalesOpcionProfesional.removeAllViews()

                for (empleado in listaEmpleadosDisponiblesOpcionProfesional) {
                    val chip =
                        layoutInflater.inflate(
                            layout.single_chip_layout,
                            binding.chipGroupProfesionalesOpcionHora,
                            false
                        ) as Chip
                    chip.text = "${empleado.nombre} ${empleado.apellidos}"
                    chip.id = empleado.idUsuario!!
                    binding.chipGroupProfesionalesOpcionProfesional.addView(chip)
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IllegalStateException) {
                Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                    .show()

                navegarInicio()
            }
        }
    }

    /**
     * Obtiene los servicios disponibles por cada empleado y rellena los chips.
     */
    fun cargarServiciosPorEmpleado() {
        var job: ArrayList<Servicio>
        CoroutineScope(Dispatchers.Main).launch {

            try {

                job = ApiRestAdapter.cargarServiciosPorEmpleado(idEmpleadoSeleccionado).await()

                listaServiciosPorEmpleado = job

                if (opcionCita == OpcionesCita.HORA) {
                    binding.bannerServicioOpcionHora.visibility = View.VISIBLE
                    binding.chipGroupServiciosOpcionHora.visibility = View.VISIBLE
                    binding.chipGroupServiciosOpcionHora.clearCheck()
                    binding.chipGroupServiciosOpcionHora.removeAllViews()
                } else {
                    binding.bannerServicioOpcionProfesional.visibility = View.VISIBLE
                    binding.chipGroupServiciosOpcionProfesional.visibility = View.VISIBLE
                    binding.chipGroupServiciosOpcionProfesional.clearCheck()
                    binding.chipGroupServiciosOpcionProfesional.removeAllViews()
                }

                var chip: Chip
                for (servicio in listaServiciosPorEmpleado) {
                    if (opcionCita == OpcionesCita.HORA) {
                        chip =
                            layoutInflater.inflate(
                                layout.single_chip_layout,
                                binding.chipGroupServiciosOpcionHora,
                                false
                            ) as Chip
                    } else {
                        chip =
                            layoutInflater.inflate(
                                layout.single_chip_layout,
                                binding.chipGroupServiciosOpcionProfesional,
                                false
                            ) as Chip
                    }

                    chip.text = "${servicio.nombre} - ${servicio.precio.toString()}€"
                    chip.id = servicio.idServicio!!

                    if (opcionCita == OpcionesCita.HORA) {
                        binding.chipGroupServiciosOpcionHora.addView(chip)
                    } else {
                        binding.chipGroupServiciosOpcionProfesional.addView(chip)
                    }

                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IllegalStateException) {
                Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                    .show()

                navegarInicio()
            }
        }
    }

    /**
     * Obtiene los horarios disponibles por fecha y rellena los chips.
     */
    fun cargarHorariosLibresDia() {
        var job: ArrayList<Horario>
        CoroutineScope(Dispatchers.Main).launch {

            try {

                job = ApiRestAdapter.cargarHorariosLibresDia(fechaSeleccionada).await()

                listaHorariosLibres = job

                binding.bannerHoraOpcionHora.visibility = View.VISIBLE
                binding.chipGroupHoraOpcionHora.visibility = View.VISIBLE
                binding.chipGroupHoraOpcionHora.clearCheck()
                binding.chipGroupHoraOpcionHora.removeAllViews()

                for (hora in listaHorariosLibres) {
                    val chip =
                        layoutInflater.inflate(
                            layout.single_chip_layout,
                            binding.chipGroupHoraOpcionHora,
                            false
                        ) as Chip
                    chip.text = hora.hora?.substringBeforeLast(":")
                    chip.id = hora.idHorario!!
                    binding.chipGroupHoraOpcionHora.addView(chip)
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IllegalStateException) {
                Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                    .show()

                navegarInicio()
            }
        }
    }

    /**
     * Obtiene los horarios disponibles por empleado y fecha y rellena los chips.
     */
    fun cargarHorariosLibresEmpleadosFecha() {
        var job: ArrayList<Horario>
        CoroutineScope(Dispatchers.Main).launch {

            try {

                job = ApiRestAdapter.cargarHorariosLibresEmpleadosFecha(
                    idEmpleadoSeleccionado,
                    fechaSeleccionada
                ).await()

                listaHorariosLibres = job

                binding.bannerHoraOpcionProfesional.visibility = View.VISIBLE
                binding.chipGroupHoraOpcionProfesional.visibility = View.VISIBLE
                binding.chipGroupHoraOpcionProfesional.clearCheck()
                binding.chipGroupHoraOpcionProfesional.removeAllViews()

                for (hora in listaHorariosLibres) {
                    val chip =
                        layoutInflater.inflate(
                            layout.single_chip_layout,
                            binding.chipGroupHoraOpcionProfesional,
                            false
                        ) as Chip
                    chip.text = hora.hora?.substringBeforeLast(":")
                    chip.id = hora.idHorario!!
                    binding.chipGroupHoraOpcionProfesional.addView(chip)
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IllegalStateException) {
                Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                    .show()

                navegarInicio()
            }
        }
    }

    /**
     * Obtiene el listado de clientes y settea el spinner con esos datos.
     */
    fun cargarClientes() {
        var job: ArrayList<Usuario>
        CoroutineScope(Dispatchers.Main).launch {

            try {

                job = ApiRestAdapter.cargarClientes().await()

                model.setClientes(job)

                setSpinner()

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IllegalStateException) {
                Toast.makeText(activity, "Debe reiniciar la sesión", Toast.LENGTH_LONG)
                    .show()

                navegarInicio()
            }
        }
    }

    /**
     * Añade una nueva cita. Si la añade el empleado, se usará el id del cliente seleccionado. Si es el cliente, se usará
     * su id de usuario.
     */
    fun addCita() {

        CoroutineScope(Dispatchers.Main).launch {

            try {

                val resultado: MensajeGeneral?

                if (model.rol != Roles.CLIENTE) {
                    resultado = ApiRestAdapter.addCita(
                        idHorarioSeleccionado,
                        idEmpleadoSeleccionado,
                        fechaSeleccionada,
                        idClienteSeleccionado,
                        cadenaStringServicios
                    ).await()
                } else {
                    resultado = ApiRestAdapter.addCita(
                        idHorarioSeleccionado, idEmpleadoSeleccionado, fechaSeleccionada,
                        model.getUsuario.value!!.idUsuario!!, cadenaStringServicios
                    ).await()
                }

                if (resultado.mensaje.equals("Registro insertado")) {

                    Toast.makeText(
                        requireContext(),
                        "Cita añadida",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        activity,
                        "No se ha podido añadir",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                reseteaOpcionesModoHora()
                reseteaOpcionesModoProfesional()

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Resetea los objetos y limpia listas tras añadir una cita en la opción de hora.
     */
    private fun reseteaOpcionesModoHora() {
        binding.chipGroupHoraOpcionHora.clearCheck()
        binding.chipGroupProfesionalesOpcionHora.clearCheck()
        binding.chipGroupServiciosOpcionHora.clearCheck()
        binding.chipGroupHoraOpcionHora.removeAllViews()
        binding.chipGroupProfesionalesOpcionHora.removeAllViews()
        binding.chipGroupServiciosOpcionHora.removeAllViews()
        binding.chipGroupHoraOpcionHora.visibility = View.GONE
        binding.chipGroupServiciosOpcionHora.visibility = View.GONE
        binding.chipGroupProfesionalesOpcionHora.visibility = View.GONE
        binding.bannerHoraOpcionHora.visibility = View.GONE
        binding.bannerProfesionalOpcionHora.visibility = View.GONE
        binding.bannerServicioOpcionHora.visibility = View.GONE
        fechaSeleccionada = ""
        fechaSeleccionadaInput.setText(getString(string.elige_fecha))
    }

    /**
     * Resetea los objetos y limpia listas tras añadir una cita en la opción de profesional.
     */
    private fun reseteaOpcionesModoProfesional() {
        binding.chipGroupHoraOpcionProfesional.clearCheck()
        binding.chipGroupProfesionalesOpcionProfesional.clearCheck()
        binding.chipGroupServiciosOpcionProfesional.clearCheck()
        binding.chipGroupHoraOpcionProfesional.removeAllViews()
        binding.chipGroupProfesionalesOpcionProfesional.removeAllViews()
        binding.chipGroupServiciosOpcionProfesional.removeAllViews()
        binding.chipGroupHoraOpcionProfesional.visibility = View.GONE
        binding.chipGroupServiciosOpcionProfesional.visibility = View.GONE
        binding.chipGroupProfesionalesOpcionProfesional.visibility = View.GONE
        binding.bannerHoraOpcionProfesional.visibility = View.GONE
        binding.bannerProfesionalOpcionProfesional.visibility = View.GONE
        binding.bannerServicioOpcionProfesional.visibility = View.GONE
        fechaSeleccionada = ""
        fechaSeleccionadaInput.setText(getString(string.elige_fecha))
    }

    /**
     * Establece el spinner con sus datos
     */
    private fun setSpinner() {

        val clientes = model.getClientes().value!!
        for (i in clientes.indices) {
            listaCitasString.add(clientes[i].nombre!! + " " + clientes[i].apellidos + " - " + clientes[i].telefono)
        }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            listaCitasString
        )

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
    }

    /**
     * Obtiene el id del cliente seleccionado en el spinner.
     */
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        idClienteSeleccionado = model.getClientes().value!![p2].idUsuario!!
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    /**
     * Obtiene la fecha seleccionada para la cita.
     */
    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {

        val date = dayOfMonth.toString() + "/" + (monthOfYear + 1).toString() + "/" + year
        val mes = (monthOfYear + 1).toString()
        var mesSeleccionado = ""
        if (mes.length == 1) {
            mesSeleccionado = "0" + (monthOfYear + 1).toString()
        } else {
            mesSeleccionado = (monthOfYear + 1).toString()
        }
        fechaSeleccionada = "$year-$mesSeleccionado-$dayOfMonth"
        fechaSeleccionadaInput.setText(date)

        if (opcionCita == OpcionesCita.HORA) {
            cargarHorariosLibresDia()
        } else {
            cargarEmpleadosDisponiblesOpcionProfesional()
        }
    }

    fun navegarInicio() {
        val contextoFragment = this
        MainActivity.autorizado = false
        val navController = NavHostFragment.findNavController(contextoFragment)
        navController.navigate(com.pacogarcia.proyectopeluqueria.R.id.action_global_fragmentInicio2)
    }
}

