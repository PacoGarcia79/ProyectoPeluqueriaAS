package com.pacogarcia.proyectopeluqueria

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.pacogarcia.proyectopeluqueria.clasesestaticas.Hash
import com.pacogarcia.proyectopeluqueria.databinding.FragmentRecordarBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.util.*
import javax.mail.internet.InternetAddress


class FragmentRecordar : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentRecordarBinding
    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val STRING_LENGTH = 6

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordarBinding.inflate(inflater, container, false)

        binding.resetBoton.setOnClickListener(this)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0) {
            binding.resetBoton -> {

                if(!binding.emailInput.text.toString().equals("")){
                    getUsernameFromEmail(binding.emailInput.text.toString())
                }
                else{
                    Toast.makeText(
                        activity,
                        "Introduzca el email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    /**
     * Obtiene el nombre de usuario a partir del email
     *
     * @param email
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getUsernameFromEmail(email: String) {

        CoroutineScope(Dispatchers.Main).launch {

            try {

                val usuario = ApiRestAdapter.getUsernameFromEmail(email).await()

                if (!usuario.username.equals("")) {

                    val randomString = (1..STRING_LENGTH)
                        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                        .map(charPool::get)
                        .joinToString("");

                    val randomCodified = Hash.hash(randomString)

                    modificarPassword(email, randomString, randomCodified, usuario.username!!)

                } else {
                    Toast.makeText(
                        activity,
                        "No existe un usuario con ese email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Este método se usa para modificar la contraseña de un usuario partiendo de su email.
     *
     * @param email
     * @param password nueva contraseña
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun modificarPassword(email: String, randomPassword: String, password: String, username: String) {

        val contexto = this
        CoroutineScope(Dispatchers.Main).launch {

            try {

                val resultado = ApiRestAdapter.modificarPassword(email, password).await()

                if (resultado.mensaje.equals("Registro actualizado")) {

                    //enviar email
                    val auth = EmailService.UserPassAuthenticator("peluqueriaclick@outlook.es", String(Base64.getDecoder().decode("UHJveWVjdG8xMjM0Iw==")))
                    val to = listOf(InternetAddress(email))
                    val from = InternetAddress("peluqueriaclick@outlook.es")
                    val email = EmailService.Email(auth, to, from, "Recordatorio usuario Peluquería en un click", "¡Hola!\nTu usuario es "
                            + username + ", y tu nueva contraseña es " + randomPassword + "\nYa puedes iniciar sesión de nuevo.")
                    val emailService = EmailService("smtp.office365.com", 587)

                    CoroutineScope(Dispatchers.Main).launch{
                        emailService.send(email)
                    }

                    Toast.makeText(
                        requireContext(),
                        "¡Email enviado! Revisa tu bandeja de entrada.",
                        Toast.LENGTH_LONG
                    ).show()

                    val navController = NavHostFragment.findNavController(contexto)
                    navController.navigate(R.id.action_fragmentRecordar_to_fragmentInicio2)

                } else {
                    Toast.makeText(
                        activity,
                        "No se ha podido enviar",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }


}