package com.pacogarcia.proyectopeluqueria

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.pacogarcia.proyectopeluqueria.databinding.FragmentInicioBinding

/**
 * Fragmento de inicio
 */
class FragmentInicio : Fragment() {

    private lateinit var binding : FragmentInicioBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentInicioBinding.inflate(inflater, container, false)

        // Si el usuario no está autorizado, abre el diálogo de login
        if(!MainActivity.autorizado){
            abreDialogo()
        }

        return binding.root
    }

    /**
     * Abre el diálogo de login
     */
    fun abreDialogo(){
        val navController = NavHostFragment.findNavController(this)
        if (navController.currentDestination?.id == R.id.fragmentInicio2) {
            navController.navigate(R.id.action_fragmentInicio2_to_dialogoLogin2)
        }
    }

    /**
     * Evita que se gire la pantalla mientras se muestra el diálogo de login
     */
    override fun onResume() {
        super.onResume()
        if(!MainActivity.autorizado){
            activity?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)
    }
}