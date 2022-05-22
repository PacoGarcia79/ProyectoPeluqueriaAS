package com.pacogarcia.proyectopeluqueria

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import com.google.android.material.navigation.NavigationView
import com.pacogarcia.proyectopeluqueria.databinding.ActivityMainBinding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.AppBarLayout
import com.pacogarcia.proyectopeluqueria.dialogos.DialogoConfirmacion
import com.pacogarcia.proyectopeluqueria.modelos.ProductoGrupo
import com.pacogarcia.proyectopeluqueria.modelos.Roles
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException


class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    DialogoConfirmacion.ConfirmacionDialogologListener {
    private lateinit var binding: ActivityMainBinding
    private val model: ItemViewModel by viewModels()

    lateinit var toolbar: Toolbar
    lateinit var appBarLayout: AppBarLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawer_layout: DrawerLayout
    lateinit var navController: NavController
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHostFragment: NavHostFragment
    var cadena = ""
    var opcionOverflow = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.contenedorMain) as NavHostFragment

        navController =
            NavHostFragment.findNavController(navHostFragment.childFragmentManager.fragments[0])

        drawer_layout = binding.drawerLayout
        toolbar = binding.toolbar
        appBarLayout = binding.appBarLayout
        toolbar.title = ""

        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(
            this, drawer_layout,
            binding.toolbar, R.string.navigation_open,
            R.string.navigation_close
        )
        drawer_layout.addDrawerListener(toggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    fun disableDrawer() {
        drawer_layout.close()
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun enableDrawer() {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun onConfigurationChanged(
        newConfig: android.content.
        res.Configuration
    ) {

        if (newConfig != null) {
            super.onConfigurationChanged(newConfig)
        }
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragmentInicio2 -> {
                navController.navigate(R.id.action_global_fragmentInicio2)
            }
            R.id.fragmentProductos -> {
                navController.navigate(R.id.action_global_fragmentProductos4)
            }
            R.id.fragmentCita -> {
                navController.navigate(R.id.action_global_fragmentCita)
            }
            R.id.fragmentHorarios -> {
                navController.navigate(R.id.action_global_fragmentHorarios2)
            }
            R.id.fragmentReservas -> {
                navController.navigate(R.id.action_global_fragmentReservas)
            }
            R.id.fragmentCuenta -> {
                navController.navigate(R.id.action_global_fragmentCuenta)
            }
        }

        cierraDrawer()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                cadena = "¿Está seguro que quiere cerrar la sesión?"
                opcionOverflow = 1
                abreDialogoConfirmacion()
            }
            R.id.salir -> {
                cadena = "¿Está seguro que desea salir?"
                opcionOverflow = 2
                abreDialogoConfirmacion()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.fragmentInicio2 && autorizado) {
            cadena = "¿Está seguro que quiere cerrar la sesión?"
            opcionOverflow = 1
            abreDialogoConfirmacion()
        } else {
            navController.popBackStack()
        }
    }

    fun salir() {
        finishAndRemoveTask()
    }

    /**
     * Abre el diálogo de confirmación para las opciones de logout y salir
     */
    private fun abreDialogoConfirmacion() {

        val bundle = Bundle().apply {
            putString("mensaje", cadena)
        }

        navController.navigate(R.id.action_global_dialogoConfirmacion, bundle)
    }

    fun modificaDrawer() {
        navigationView.menu.clear()
        if (model.getUsuario.value?.rol == Roles.CLIENTE) {
            navigationView.inflateMenu(R.menu.menu_drawer)
        } else {
            navigationView.inflateMenu(R.menu.menu_drawer_empleado)
        }
    }

    fun cierraDrawer() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * Acciones que se realizan si se hace click en el botón aceptar del diálogo de confirmación
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {

        when (opcionOverflow) {
            1 -> logout()
            2 -> salir()
        }
    }

    /**
     * Cierra la sesión
     */
    private fun logout() {

        CoroutineScope(Dispatchers.Main).launch {

            try {

                val resultado = ApiRestAdapter.logout().await()

                if (resultado.usuario!!.isEmpty()) {

                    autorizado = false

                    Toast.makeText(
                        applicationContext,
                        "Has cerrado la sesión",
                        Toast.LENGTH_SHORT
                    ).show()

                    cierraDrawer()

                    navController.navigate(R.id.action_global_fragmentInicio2)
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(
                    applicationContext,
                    "Error al acceder a la base de datos",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }


    companion object {
        var productoGrupos: ArrayList<ProductoGrupo>? = null
        var productoGruposCargados = false
        var autorizado: Boolean = false
        var clickAddProductoCitaHolder = false
        var dialogoAbiertoDesdeReservas = false
        var dialogoAbiertoDesdeHorarios = false
        var rol: Roles? = null
    }


}
