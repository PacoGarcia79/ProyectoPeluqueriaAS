package com.pacogarcia.proyectopeluqueria

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pacogarcia.proyectopeluqueria.clasesestaticas.FechasHorasUtilidad
import com.pacogarcia.proyectopeluqueria.clasesrecycler.AdaptadorReservas
import com.pacogarcia.proyectopeluqueria.clasesrecycler.MyItemDetailsLookupReservas
import com.pacogarcia.proyectopeluqueria.clasesestaticas.FechasHorasUtilidad.formatLocalDateTimeParaMySQL
import com.pacogarcia.proyectopeluqueria.modelos.Cita
import com.pacogarcia.proyectopeluqueria.modelos.Roles
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import com.pacogarcia.proyectopeluqueria.databinding.FragmentReservasBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Fragmento para la gestión de las reservas
 */
class FragmentReservas : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentReservasBinding
    private lateinit var fabSearch: FloatingActionButton
    private val model: ItemViewModel by activityViewModels()
    private var fechaComienzoPeriodo: Long = 0
    private var fechaFinPeriodo: Long = 0

    private var tracker: SelectionTracker<Long>? = null
    private var actionMode: ActionMode? = null
    private lateinit var adaptador: AdaptadorReservas
    private lateinit var recycler: RecyclerView

    private lateinit var actividadPrincipal: MainActivity
    var citas: ArrayList<Cita> = ArrayList()
    var cadenaDialogoConfirmacion = ""
    var listasString = ""
    var lista: List<Long>? = null

    /**
     * Implementación de ActionMode.Callback, que permite que se puedan cancelar
     * los elementos seleccionados mediante el botón que se muestra en la action bar
     */
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu):
                Boolean {

            actividadPrincipal.menuInflater.inflate(R.menu.delete_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu):
                Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem):
                Boolean {
            return when (item.itemId) {
                R.id.delete -> {
                    lista = tracker?.selection?.sorted()?.reversed()!!

                    listasString = obtieneListaStringCitas(lista!!)

                    var cantidadCitas = if (lista!!.size > 1) "las citas?" else "la cita?"
                    cadenaDialogoConfirmacion = "¿Está seguro que quiere cancelar " + cantidadCitas
                    abreDialogoConfirmacion()

                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            tracker?.clearSelection()
            actionMode = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        actividadPrincipal = (requireActivity() as MainActivity)

        // Guarda el estado del tracker en la actividad
        if (savedInstanceState != null)
            tracker?.onRestoreInstanceState(savedInstanceState)

        binding = FragmentReservasBinding.inflate(inflater, container, false)
        recycler = binding.recyclerList

        fabSearch = binding.fabSearch

        fabSearch.setOnClickListener(this)

        val idUsuario = model.getUsuario.value?.idUsuario!!

        val fechaInicio = LocalDateTime.now()
        val fechaFin = LocalDateTime.now().plusDays(60)

        // Carga las citas entre la fecha actual y sesenta días después para el usuario, dependiendo del rol
        getCitas(
            formatLocalDateTimeParaMySQL(fechaInicio),
            formatLocalDateTimeParaMySQL(fechaFin),
            idUsuario
        )

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("reservasKey") { requestKey, bundle ->
            val result = bundle.getBoolean("bundleReservas")
            if (result) {
                cancelaCitas()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0) {
            // Carga el date picker con selección de rango de fechas y refresca el listado de citas según las nuevas fechas
            fabSearch -> {
                val constraintsBuilder = CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now())

                val dateRangePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                        .setCalendarConstraints(constraintsBuilder.build())
                        .setTitleText("Elija las fechas")
                        .setSelection(
                            androidx.core.util.Pair(
                                LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(),
                                LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.UTC)
                                    .toEpochMilli()
                            )
                        )
                        .build()

                dateRangePicker.show(requireActivity().supportFragmentManager, "datePicker")

                dateRangePicker.addOnPositiveButtonClickListener {
                    fechaComienzoPeriodo = it.first
                    fechaFinPeriodo = it.second

                    val idUsuario = model.getUsuario.value?.idUsuario!!
                    val inicioRangoFecha =
                        FechasHorasUtilidad.convertirFechaMillAString(fechaComienzoPeriodo)
                    val finRangoFecha =
                        FechasHorasUtilidad.convertirFechaMillAString(fechaFinPeriodo)

                    refrescaListaCitas(inicioRangoFecha, finRangoFecha, idUsuario)
                }
            }
        }
    }

    /**
     * A partir del listado del tracker obtiene una cadena con los ids reales de cada uno de los elementos
     *
     * @param lista lista de números de tipo long creada por el tracker y que va modificando conforme se seleccionan o deseleccionan
     * elementos
     * @return cadena string con los ids
     */
    fun obtieneListaStringCitas(lista: List<Long>): String {
        val sb = StringBuilder()
        val listaIdCitas: ArrayList<Int> = ArrayList()

        for (i in lista.indices) {
            listaIdCitas.add(citas[i].idCita!!)
        }

        for (i in listaIdCitas.indices) {
            if (i < listaIdCitas.size - 1)
                sb.append(listaIdCitas[i].toString()).append(",")
            else sb.append(listaIdCitas[i].toString())
        }

        return sb.toString()
    }

    /**
     * Establece el tracker y añade un observer para mostrar el número de elementos seleccionados
     */
    fun setTracker() {

        tracker = SelectionTracker.Builder(
            "mySelection",
            recycler,
            StableIdKeyProvider(recycler),
            MyItemDetailsLookupReservas(recycler),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    if (tracker!!.hasSelection()) {
                        if (actionMode == null) {
                            actionMode =
                                actividadPrincipal.startSupportActionMode(actionModeCallback)
                        }
                        actionMode?.title = "${tracker!!.selection.size()}"
                    } else {
                        actionMode?.finish()
                    }
                }
            })

        adaptador.setTracker(tracker)
    }

    /**
     * Inicia el adaptador y el recycler
     *
     * @param datos array de datos para el adaptador
     */
    fun iniciaAdaptadorRecycler(datos: ArrayList<Cita>) {

        adaptador = AdaptadorReservas(datos, requireActivity())
        recycler.setHasFixedSize(true)
        recycler.adapter = adaptador
        recycler.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        adaptador.onClick(this)

        setTracker()
    }

    /**
     * Obtiene las citas de confirmadas según el rol del usuario, e inicia el adaptador
     *
     * @param fechaInicio fecha inicio del periodo
     * @param fechaFin fecha fin del periodo
     * @param idUsuario id del usuario
     */
    fun getCitas(fechaInicio: String, fechaFin: String, idUsuario: Int) {
        var job: ArrayList<Cita>

        CoroutineScope(Dispatchers.Main).launch {

            try {
                job = when (model.rol) {
                    Roles.ADMIN -> {
                        ApiRestAdapter.cargarCitas(fechaInicio, fechaFin, 0).await()
                    }
                    else -> {
                        ApiRestAdapter.cargarCitas(fechaInicio, fechaFin, idUsuario).await()
                    }
                }

                citas = job

                if (citas.size == 0) {
                    muestraSnackBarCitas(fechaInicio, fechaFin)
                }

                iniciaAdaptadorRecycler(citas)

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
     * Cancela las citas seleccionadas
     *
     */
    private fun cancelaCitas() {
        CoroutineScope(Dispatchers.Main).launch {

            try {
                val resultado = ApiRestAdapter.cancelarCitas(listasString).await()

                if (resultado.mensaje.equals("Registro/s actualizado/s")) {

                    lista?.forEach { id ->
                        citas.removeAt(id.toInt())
                    }
                    recycler.recycledViewPool.clear()
                    adaptador.notifyDataSetChanged()
                    tracker?.clearSelection()
                    actionMode = null

                    Snackbar.make(
                        binding.root,
                        "Cita/s cancelada/s",
                        Snackbar.LENGTH_LONG
                    ).show()

                } else {
                    Snackbar.make(
                        binding.root,
                        "No se ha podido cancelar",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Actualiza el listado de citas confirmadas según el rol del usuario, y a su vez actualiza el adaptador
     *
     * @param fechaInicio fecha inicio del periodo
     * @param fechaFin fecha fin del periodo
     * @param idUsuario id del usuario
     */
    fun refrescaListaCitas(fechaInicio: String, fechaFin: String, idUsuario: Int) {
        var job: ArrayList<Cita>
        CoroutineScope(Dispatchers.Main).launch {

            try {
                job = when (model.rol) {
                    Roles.ADMIN -> {
                        ApiRestAdapter.cargarCitas(fechaInicio, fechaFin, 0).await()
                    }
                    else -> {
                        ApiRestAdapter.cargarCitas(fechaInicio, fechaFin, idUsuario).await()
                    }
                }

                citas = job

                if (citas.size == 0) {
                    muestraSnackBarCitas(fechaInicio, fechaFin)
                }

                updateRecyclerData(citas)
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
     * Muestra el snackbar si no hay citas confirmadas en ese periodo
     *
     * @param fechaInicio fecha inicio del periodo
     * @param fechaFin fecha fin del periodo
     */
    private fun muestraSnackBarCitas(fechaInicio: String, fechaFin: String) {
        val periodoCadenaSnackbar: String = if (fechaInicio == fechaFin) "día" else "periodo"
        Snackbar.make(
            binding.root,
            "No hay citas confirmadas en este $periodoCadenaSnackbar",
            Snackbar.LENGTH_LONG
        ).show()
    }

    /**
     * Actualiza el adaptador con los nuevos datos
     *
     * @param citas nuevo array de datos para el adaptador
     */
    fun updateRecyclerData(citas: ArrayList<Cita>) {
        adaptador.setData(citas)
        adaptador.notifyDataSetChanged()
    }

    /**
     * Abre el diálogo de confirmación para las opciones de logout y salir
     */
    private fun abreDialogoConfirmacion() {

        MainActivity.dialogoAbiertoDesdeReservas = true
        val contexto = this

        val bundle = Bundle().apply {
            putString("mensaje", cadenaDialogoConfirmacion)
        }

        val navController = NavHostFragment.findNavController(contexto)
        navController.navigate(R.id.action_global_dialogoConfirmacion, bundle)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState != null)
            tracker?.onSaveInstanceState(outState)
    }

    fun navegarInicio() {
        val contextoFragment = this
        MainActivity.autorizado = false
        val navController = NavHostFragment.findNavController(contextoFragment)
        navController.navigate(R.id.action_global_fragmentInicio2)
    }
}
