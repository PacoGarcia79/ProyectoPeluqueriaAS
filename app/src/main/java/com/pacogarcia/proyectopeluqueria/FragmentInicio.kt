package com.pacogarcia.proyectopeluqueria

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.pacogarcia.proyectopeluqueria.databinding.FragmentInicioBinding
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel

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

//        val navController = NavHostFragment.findNavController(this)
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            if (destination.id == R.id.fragmentB) {
//                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//            } else if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR) {
//                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
//            }
//        }


        // Si el usuario no está autorizado, abre el diálogo de login
        if(!MainActivity.autorizado){
            abreDialogo()
        }

        //TODO: foto al principio??
        if (context?.resources!!
                .configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            binding.fotoPeluqueria.setPadding(15, 50, 15, 0)
        } else {
            binding.fotoPeluqueria.setPadding(40, 0, 40, 0)
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        setImagePadding(newConfig)
    }

    private fun setImagePadding(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            binding.fotoPeluqueria.setPadding(15, 50, 15, 0)
        } else {
            binding.fotoPeluqueria.setPadding(40, 0, 40, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        if(!MainActivity.autorizado){
            activity?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }
}