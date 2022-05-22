package com.pacogarcia.proyectopeluqueria.dialogos

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pacogarcia.proyectopeluqueria.ApiRestAdapter
import com.pacogarcia.proyectopeluqueria.MainActivity
import com.pacogarcia.proyectopeluqueria.R
import com.pacogarcia.proyectopeluqueria.clasesestaticas.Hash
import com.pacogarcia.proyectopeluqueria.modelos.Roles
import com.pacogarcia.proyectopeluqueria.modelos.Usuario
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import kotlin.system.exitProcess

/**
 * Diálogo para el login o el registro.
 */
class DialogoLogin : DialogFragment(), View.OnClickListener {

    private val model: ItemViewModel by activityViewModels()
    private lateinit var actividadPrincipal: MainActivity
    private lateinit var crear: TextView
    private lateinit var recordar: TextView
    private lateinit var entrar: Button
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var contextoFragment: DialogFragment
    private lateinit var v: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = MaterialAlertDialogBuilder(requireActivity())
        val inflater = requireActivity().layoutInflater
        v = inflater.inflate(R.layout.login_dialogo, null)
        builder.setView(v)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        crear = v.findViewById<View>(R.id.registrar_text) as TextView
        recordar = v.findViewById<View>(R.id.recordar_text) as TextView
        entrar = v.findViewById<View>(R.id.iniciar_sesion_boton) as Button
        username = v.findViewById(R.id.nombre_input)
        password = v.findViewById(R.id.contra_input)

        contextoFragment = this

        crear.setOnClickListener(this)
        entrar.setOnClickListener(this)
        recordar.setOnClickListener(this)

        actividadPrincipal = (requireActivity() as MainActivity)

        actividadPrincipal.disableDrawer()

        dialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode === KeyEvent.KEYCODE_BACK
                && event.getAction() === KeyEvent.ACTION_UP
            ) {
                exitProcess(1)
                //return@setOnKeyListener true
            }
            false
        }

        return dialog
    }

    fun comprobarDatosEntrada(view: View, nombre: String, password: String): Boolean {
        if (nombre.isNullOrEmpty() || password.isNullOrEmpty()) {
            Toast.makeText(activity, "Introduzca nombre y contraseña", Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            entrar -> {
                if (comprobarDatosEntrada(v, username.text.toString(), password.text.toString())) {
                    val usuario = Usuario(
                        username.text.toString(),
                        Hash.hash(password.text.toString())
                    )

                    autorizar(usuario)
                }
            }
            crear -> {
                val navController = NavHostFragment.findNavController(contextoFragment)
                navController.navigate(R.id.action_dialogoLogin_to_fragmentRegistrar)
                this.dismiss()
            }
            recordar ->{
                val navController = NavHostFragment.findNavController(contextoFragment)
                navController.navigate(R.id.action_dialogoLogin_to_fragmentRecordar)
                this.dismiss()
            }
        }
    }

    /**
     * Autoriza al usuario y guarda los datos que se usarán para identificarlo a la hora de mostrar las distintas
     * opciones en el menú drawer.
     *
     * @param usuario Usuario a identificar
     */
    private fun autorizar(usuario: Usuario) {
        CoroutineScope(Dispatchers.Main).launch {
            try{
                val resultado = ApiRestAdapter.autorizaUsuario(
                    usuario
                ).await()

                if (!resultado.usuario!!.isEmpty()) {

                    MainActivity.autorizado = true

                    usuario.nombre = resultado.nombre
                    usuario.idUsuario = resultado.idUsuario
                    usuario.rol = enumValueOf<Roles>(resultado.rol.toString().uppercase())
                    model.rol = usuario.rol
                    MainActivity.rol = usuario.rol
                    model.setUsuario(usuario)
                    getUsuarioIdentificado(resultado.idUsuario!!)

                    val navController = NavHostFragment.findNavController(contextoFragment)

                    actividadPrincipal.modificaDrawer()
                    actividadPrincipal.enableDrawer()

                    Toast.makeText(
                        requireContext(),
                        "¡Bienvenido/a, " + usuario.nombre + "!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate(R.id.action_dialogoLogin_to_fragmentInicio2)

                } else {
                    Toast.makeText(activity, "Datos erróneos", Toast.LENGTH_SHORT).show()
                }
            }
            catch(e: SocketTimeoutException){
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * Obtiene los datos del usuario a partir de su id
     *
     * @param idUsuario id del usuario
     */
    fun getUsuarioIdentificado(idUsuario: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val job = ApiRestAdapter.getUserData(idUsuario).await()
            model.setUsuario(job)
        }
    }




}