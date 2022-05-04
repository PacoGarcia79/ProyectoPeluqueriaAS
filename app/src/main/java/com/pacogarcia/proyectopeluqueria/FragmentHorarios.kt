package com.pacogarcia.proyectopeluqueria

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pacogarcia.proyectopeluqueria.clasesestaticas.FechasHorasUtilidad
import com.pacogarcia.proyectopeluqueria.clasesrecycler.AdaptadorDisponibilidad
import com.pacogarcia.proyectopeluqueria.clasesrecycler.MyItemDetailsLookupDisponibilidad
import com.pacogarcia.proyectopeluqueria.databinding.FragmentHorariosBinding
import com.pacogarcia.proyectopeluqueria.modelos.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


class FragmentHorarios : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentHorariosBinding

    private lateinit var fechasSeleccionadas: TextView
    private lateinit var fabHabilita: FloatingActionButton
    private lateinit var fabDeshabilita: FloatingActionButton

    private var fechaComienzoPeriodo: String = ""
    private var fechaFinPeriodo: String = ""
    private var fechaComienzoMostrar: String = ""
    private var fechaFinMostrar: String = ""

    var listaEmpleados: ArrayList<Usuario> = ArrayList()
    var listaHorarios: ArrayList<Horario> = ArrayList()
    var listaNoDisponibilidad: ArrayList<Disponibilidad> = ArrayList()

    private lateinit var actividadPrincipal: MainActivity
    private var tracker: SelectionTracker<Long>? = null
    private var actionMode: ActionMode? = null
    private lateinit var adaptador: AdaptadorDisponibilidad
    private lateinit var recycler: RecyclerView

    var cadenaStringHorarios: String = ""
    var cadenaStringEmpleados: String = ""

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu):
                Boolean {

            actividadPrincipal.menuInflater.inflate(R.menu.delete_disponibilidad_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu):
                Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem):
                Boolean {
            return when (item.itemId) {
                R.id.deshabilita -> {
                    val lista : List<Long> = tracker?.selection?.sorted()?.reversed()!!

                    val listasString = obtieneListaStringDisponibilidad(lista)

                    deshabilitaDisponibilidadIds(listasString, lista)

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentHorariosBinding.inflate(inflater, container, false)

        if (savedInstanceState != null)
            tracker?.onRestoreInstanceState(savedInstanceState)

        recycler = binding.recyclerList
        actividadPrincipal = (requireActivity() as MainActivity)

        fechasSeleccionadas = binding.fechasSeleccionadas
        fechasSeleccionadas.setOnClickListener(this)

        fabHabilita = binding.fabHabilita
        fabHabilita.setOnClickListener(this)

        fabDeshabilita = binding.fabDeshabilita
        fabDeshabilita.setOnClickListener(this)

        cargarHorariosDeshabilitados()
        cargarEmpleados()
        cargarHorarios()

        return binding.root
    }

    fun obtieneListaStringDisponibilidad(lista : List<Long>) : String{
        val sb = StringBuilder()
        val listaIdDisponibilidad : ArrayList<Int> = ArrayList()

        lista.forEach {
            listaIdDisponibilidad.add(listaNoDisponibilidad.get(it.absoluteValue.toInt()).idDisponibilidad!!)
        }

//        for (i in lista.indices) {
//            listaIdDisponibilidad.add(listaNoDisponibilidad[i].idDisponibilidad!!)
//        }

        for (i in listaIdDisponibilidad.indices) {
            if (i < listaIdDisponibilidad.size - 1)
                sb.append(listaIdDisponibilidad[i].toString()).append(",")
            else sb.append(listaIdDisponibilidad[i].toString())
        }

        return sb.toString()
    }

    private fun deshabilitaDisponibilidadIds(disponibilidadListaString: String, lista : List<Long>) {
        CoroutineScope(Dispatchers.Main).launch {
            val resultado = ApiRestAdapter.putDelDisponibilidadIds(disponibilidadListaString).await()

            if (resultado.mensaje.equals("Registro/s actualizado/s")) {

                lista.forEach { id ->
                    listaNoDisponibilidad.removeAt(id.toInt())
                }
                recycler.recycledViewPool.clear()
                adaptador.notifyDataSetChanged()
                tracker?.clearSelection()
                actionMode = null

                binding.chipGroupHoras.clearCheck()
                binding.chipGroupProfesionales.clearCheck()
                fechaComienzoPeriodo = ""
                fechaFinPeriodo = ""
                fechaComienzoMostrar = ""
                fechaFinMostrar = ""
                binding.fechasSeleccionadas.text = "Sin fechas seleccionadas"

                Snackbar.make(
                    binding.root,
                    "Disponibilidad Modificada",
                    Snackbar.LENGTH_LONG
                ).show()

            } else {
                Snackbar.make(
                    binding.root,
                    "Ha habido un error al modificar",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    fun setTracker() {

        tracker = SelectionTracker.Builder(
            "mySelection",
            recycler,
            StableIdKeyProvider(recycler),
            MyItemDetailsLookupDisponibilidad(recycler),
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

    fun iniciaAdaptadorRecycler(datos: ArrayList<Disponibilidad>) {

        adaptador = AdaptadorDisponibilidad(datos, requireActivity())
        recycler.setHasFixedSize(true)
        recycler.adapter = adaptador
        recycler.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        adaptador.onClick(this)

        setTracker()
    }

    fun cargarEmpleados() {
        var job: ArrayList<Usuario>
        CoroutineScope(Dispatchers.Main).launch {

            job = ApiRestAdapter.cargarEmpleados().await()

            listaEmpleados = job

            for (empleado in listaEmpleados) {
                val chip =
                    layoutInflater.inflate(
                        R.layout.single_chip_layout,
                        binding.chipGroupProfesionales,
                        false
                    ) as Chip
                chip.text = empleado.nombre
                chip.id = empleado.idUsuario!!
                binding.chipGroupProfesionales.addView(chip)
            }
        }
    }

    fun cargarHorarios() {
        var job: ArrayList<Horario>
        CoroutineScope(Dispatchers.Main).launch {

            job = ApiRestAdapter.cargarHorarios().await()

            listaHorarios = job

            for (horario in listaHorarios) {
                val chip =
                    layoutInflater.inflate(
                        R.layout.single_chip_layout,
                        binding.chipGroupHoras,
                        false
                    ) as Chip
                chip.text = horario.hora?.substringBeforeLast(":")
                chip.id = horario.idHorario!!
                binding.chipGroupHoras.addView(chip)
            }
        }
    }

    fun cargarHorariosDeshabilitados() {
        var job: ArrayList<Disponibilidad>
        CoroutineScope(Dispatchers.Main).launch {

            job = ApiRestAdapter.cargarHorariosDeshabilitados().await()

            listaNoDisponibilidad = job

            iniciaAdaptadorRecycler(listaNoDisponibilidad)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0) {
            fechasSeleccionadas -> {
                val dateRangePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Selecciona fechas")
                        .setSelection(
                            androidx.core.util.Pair(
                                MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                MaterialDatePicker.todayInUtcMilliseconds()
                            )
                        )
                        .build()

                dateRangePicker.show(requireActivity().supportFragmentManager, "datePicker")

                dateRangePicker.addOnPositiveButtonClickListener {
                    fechaComienzoPeriodo = FechasHorasUtilidad.convertirFechaMillAString(it.first)
                    fechaFinPeriodo = FechasHorasUtilidad.convertirFechaMillAString(it.second)

                    fechaComienzoMostrar =
                        FechasHorasUtilidad.convertirFechaMillAStringMostrar(it.first)
                    fechaFinMostrar =
                        FechasHorasUtilidad.convertirFechaMillAStringMostrar(it.second)

                    binding.fechasSeleccionadas.text = "$fechaComienzoMostrar - $fechaFinMostrar"
                }
            }
            fabHabilita -> {

                obtieneListadosString()

                if(cadenaStringEmpleados.isEmpty() || cadenaStringHorarios.isEmpty()
                    || fechaComienzoPeriodo.isEmpty() || fechaFinPeriodo.isEmpty()){
                    muestraDialogo()
                }else{
                    habilitaDisponibilidad()
                }
            }
            fabDeshabilita -> {

                obtieneListadosString()

                if(cadenaStringEmpleados.isEmpty() || cadenaStringHorarios.isEmpty()
                    || fechaComienzoPeriodo.isEmpty() || fechaFinPeriodo.isEmpty()){
                    muestraDialogo()
                }else{
                    deshabilitaDisponibilidad()
                }
            }
        }
    }

    private fun muestraDialogo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.gesti_n_de_horarios))
            .setMessage(resources.getString(R.string.seleccionar_datos))
            .setPositiveButton(resources.getString(R.string.aceptar)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    fun obtieneListadoString(lista: MutableList<Int>): String {
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

    fun obtieneListadosString() {
        val checkedChipHorasIds = binding.chipGroupHoras.checkedChipIds
        val checkedChipEmpleadosIds = binding.chipGroupProfesionales.checkedChipIds
        cadenaStringHorarios = obtieneListadoString(checkedChipHorasIds)
        cadenaStringEmpleados = obtieneListadoString(checkedChipEmpleadosIds)
    }

    fun deshabilitaDisponibilidad() {

        CoroutineScope(Dispatchers.Main).launch {

            var resultado: MensajeGeneral?

            resultado = ApiRestAdapter.putAddDisponibilidad(
                fechaComienzoPeriodo, fechaFinPeriodo, cadenaStringEmpleados,
                cadenaStringHorarios
            ).await()

            if (resultado.mensaje.equals("Registro actualizado")) {

                recycler.recycledViewPool.clear()
                adaptador.notifyDataSetChanged()
                tracker?.clearSelection()
                actionMode = null

                refrescaPantalla()

                Toast.makeText(
                    requireContext(),
                    "Disponibilidad Modificada",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (resultado.mensaje.equals("Error registro")) {
                Toast.makeText(
                    activity,
                    "No se puede realizar al existir previamente el registro",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    activity,
                    "Ha habido un error al modificar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun habilitaDisponibilidad() {

        CoroutineScope(Dispatchers.Main).launch {

            var resultado: MensajeGeneral?

            resultado = ApiRestAdapter.putDelDisponibilidad(
                fechaComienzoPeriodo, fechaFinPeriodo, cadenaStringEmpleados,
                cadenaStringHorarios
            ).await()

            if (resultado.mensaje.equals("Registro actualizado")) {

                recycler.recycledViewPool.clear()
                adaptador.notifyDataSetChanged()
                tracker?.clearSelection()
                actionMode = null

                refrescaPantalla()

                Toast.makeText(
                    requireContext(),
                    "Disponibilidad Modificada",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    activity,
                    "Ha habido un error al modificar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun refrescaPantalla() {
        binding.chipGroupHoras.clearCheck()
        binding.chipGroupProfesionales.clearCheck()
        fechaComienzoPeriodo = ""
        fechaFinPeriodo = ""
        fechaComienzoMostrar = ""
        fechaFinMostrar = ""
        binding.fechasSeleccionadas.text = "Sin fechas seleccionadas"

        var job: ArrayList<Disponibilidad>
        CoroutineScope(Dispatchers.Main).launch {

            job = ApiRestAdapter.cargarHorariosDeshabilitados().await()

            listaNoDisponibilidad = job

            updateRecyclerData(listaNoDisponibilidad)
        }
    }

    fun updateRecyclerData(datos: ArrayList<Disponibilidad>) {
        adaptador.setData(datos)
        adaptador.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (outState != null)
            tracker?.onSaveInstanceState(outState)
    }
}