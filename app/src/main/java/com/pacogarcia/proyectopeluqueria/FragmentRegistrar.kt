package com.pacogarcia.proyectopeluqueria

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import com.pacogarcia.proyectopeluqueria.clasesestaticas.Hash
import com.pacogarcia.proyectopeluqueria.databinding.FragmentRegistrarBinding
import com.pacogarcia.proyectopeluqueria.modelos.Usuario
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.NoSuchElementException

/**
 * Fragmento para el registro del usuario
 */
class FragmentRegistrar : Fragment(), View.OnClickListener {

    private lateinit var actividadPrincipal: MainActivity
    private lateinit var binding: FragmentRegistrarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentRegistrarBinding.inflate(inflater, container, false)

        actividadPrincipal = (requireActivity() as MainActivity)

        /**
         * Deshabilita el menú drawer para que no sea accesible desde este fragmento
         */
        actividadPrincipal.disableDrawer()

        /**
         * Informa que este fragmento va a editar las opciones del menú overflow
         */
        setHasOptionsMenu(true)

        /**
         * Programación reactiva:
         * Detecta cuando el usuario está escribiendo en los campos.
         * Ignora todos los eventos de cambio de texto que ocurren dentro de un corto espacio de tiempo, ya que esto indica que el
         * usuario todavía está escribiendo.
         * Realiza una acción cuando el usuario deje de escribir.
         *
         * @author https://code.tutsplus.com/es/tutorials/kotlin-reactive-programming-for-an-android-sign-up-screen--cms-31585
         */
        respondeEventoCambioTextoEmail()
        respondeEventoCambioTextoPasswordConfirmacion()
        respondeEventoCambioTextoNombre()
        respondeEventoCambioTextoApellidos()
        respondeEventoCambioTextoUserName()
        respondeEventoCambioTextoTelefono()
        respondeEventoCambioTextoPassword()

        binding.registrar.setOnClickListener(this)
        binding.cancelar.setOnClickListener(this)

        binding.registrar.isEnabled = false

        /**
         * Controla los cambios en los EditText para no dejar ningún campo vacío.
         * Además controla también el estado del botón registrar según sea la comprobación de los datos
         */
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                binding.registrar.isEnabled = comprobarDatos()
            }

            override fun afterTextChanged(editable: Editable) {}
        }

        binding.confirmarPasswordText.addTextChangedListener(textWatcher)
        binding.mailText.addTextChangedListener(textWatcher)
        binding.nombreText.addTextChangedListener(textWatcher)
        binding.apellidosText.addTextChangedListener(textWatcher)
        binding.userNameText.addTextChangedListener(textWatcher)
        binding.phoneText.addTextChangedListener(textWatcher)
        binding.passwordText.addTextChangedListener(textWatcher)

        return binding.root
    }

    /**
     * Responde a eventos de cambio de texto en el campo de confirmación de password
     */
    private fun respondeEventoCambioTextoPasswordConfirmacion() {
        RxTextView.afterTextChangeEvents(binding.confirmarPasswordText)
            .skipInitialValue()
            .map {
                binding.textInputConfirmPassword.error = null
                it.view().text.toString()
            }
            .debounce(400, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
            .compose(validateFieldsLength)
            .compose(validatePassword)
            .compose(retryWhenError {
                binding.textInputConfirmPassword.error = it.message
            })
            .subscribe()
    }

    /**
     * Responde a eventos de cambio de texto en el campo email
     */
    private fun respondeEventoCambioTextoEmail() {
        RxTextView.afterTextChangeEvents(binding.mailText)
            .skipInitialValue()
            .map {
                binding.textInputEmail.error = null
                it.view().text.toString()
            }
            .debounce(
                400,
                TimeUnit.MILLISECONDS
            ).observeOn(AndroidSchedulers.mainThread())
            .compose(validateFieldsLength)
            .compose(validateEmailAddress)
            .compose(retryWhenError {
                binding.textInputEmail.error = it.message
            })
            .subscribe()
    }

    /**
     * Responde a eventos de cambio de texto en el campo nombre
     */
    private fun respondeEventoCambioTextoNombre() {
        RxTextView.afterTextChangeEvents(binding.nombreText)
            .skipInitialValue()
            .map {
                binding.textInputNombre.error = null
                it.view().text.toString()
            }
            .debounce(
                400,
                TimeUnit.MILLISECONDS
            ).observeOn(AndroidSchedulers.mainThread())
            .compose(validateFieldsLength)
            .compose(retryWhenError {
                binding.textInputNombre.error = it.message
            })
            .subscribe()
    }

    /**
     * Responde a eventos de cambio de texto en el campo apellidos
     */
    private fun respondeEventoCambioTextoApellidos() {
        RxTextView.afterTextChangeEvents(binding.apellidosText)
            .skipInitialValue()
            .map {
                binding.textInputApellidos.error = null
                it.view().text.toString()
            }
            .debounce(
                400,
                TimeUnit.MILLISECONDS
            ).observeOn(AndroidSchedulers.mainThread())
            .compose(validateFieldsLength)
            .compose(retryWhenError {
                binding.textInputApellidos.error = it.message
            })
            .subscribe()
    }

    /**
     * Responde a eventos de cambio de texto en el campo nombre de usuario
     */
    private fun respondeEventoCambioTextoUserName() {
        RxTextView.afterTextChangeEvents(binding.userNameText)
            .skipInitialValue()
            .map {
                binding.textUserName.error = null
                it.view().text.toString()
            }
            .debounce(
                400,
                TimeUnit.MILLISECONDS
            ).observeOn(AndroidSchedulers.mainThread())
            .compose(validateFieldsLength)
            .compose(retryWhenError {
                binding.textUserName.error = it.message
            })
            .subscribe()
    }

    /**
     * Responde a eventos de cambio de texto en el campo telefono
     */
    private fun respondeEventoCambioTextoTelefono() {
        RxTextView.afterTextChangeEvents(binding.phoneText)
            .skipInitialValue()
            .map {
                binding.textInputPhone.error = null
                it.view().text.toString()
            }
            .debounce(
                400,
                TimeUnit.MILLISECONDS
            ).observeOn(AndroidSchedulers.mainThread())
            .compose(validateFieldsLength)
            .compose(retryWhenError {
                binding.textInputPhone.error = it.message
            })
            .subscribe()
    }

    /**
     * Responde a eventos de cambio de texto en el campo password
     */
    private fun respondeEventoCambioTextoPassword() {
        RxTextView.afterTextChangeEvents(binding.passwordText)
            .skipInitialValue()
            .map {
                binding.textInputPassword.error = null
                it.view().text.toString()
            }
            .debounce(
                400,
                TimeUnit.MILLISECONDS
            ).observeOn(AndroidSchedulers.mainThread())
            .compose(validateFieldsLength)
            .compose(retryWhenError {
                binding.textInputPassword.error = it.message
            })
            .subscribe()
    }

    /**
     * Función de transformación cuya funcion es que en caso de que surje un error, continúe con la secuencia
     */
    private inline fun retryWhenError(crossinline onError: (ex: Throwable) -> Unit): ObservableTransformer<String, String> =
        ObservableTransformer { observable ->
            observable.retryWhen { errors ->
                errors.flatMap {
                    onError(it)
                    Observable.just("")
                }
            }
        }

    /**
     * Se usa para validar que el email tenga el formato correcto, y en caso contrario mostrar el mensaje de error
     */
    private val validateEmailAddress = ObservableTransformer<String, String> { observable ->
        observable.flatMap {
            Observable.just(it).map { it.trim() }
                .filter {
                    Patterns.EMAIL_ADDRESS.matcher(it).matches()
                }
                .singleOrError()
                .onErrorResumeNext {
                    if (it is NoSuchElementException) {
                        Single.error(Exception("Indique un dirección email válida"))
                    } else {
                        Single.error(it)
                    }
                }
                .toObservable()
        }
    }

    /**
     * Se usa para validar que ambas contraseñas son iguales, y en caso contrario mostrar el mensaje de error
     */
    private val validatePassword = ObservableTransformer<String, String> { observable ->
        observable.flatMap {
            Observable.just(it).map { it.trim() }
                .filter { it.equals(binding.passwordText.text.toString()) }
                .singleOrError()
                .onErrorResumeNext {
                    if (it is NoSuchElementException) {
                        Single.error(Exception("La contraseñas no coinciden"))
                    } else {
                        Single.error(it)
                    }
                }
                .toObservable()
        }
    }

    /**
     * Se usa para comprobar la longitud de los datos introducidos. En este caso sirve para corroborar
     * que no están vacíos, y en caso contrario mostrar el mensaje de error
     */
    private val validateFieldsLength = ObservableTransformer<String, String> { observable ->
        observable.flatMap {
            Observable.just(it).map { it.trim() }
                .filter { it.isNotEmpty() }
                .singleOrError()
                .onErrorResumeNext {
                    if (it is NoSuchElementException) {
                        Single.error(Exception("Rellene el campo"))

                    } else {
                        Single.error(it)
                    }
                }
                .toObservable()
        }
    }


    /**
     * Comprueba los campos introducidos. Se usará para gestionar que el botón registrar esté o no habilitado.
     */
    fun comprobarDatos(): Boolean {
        return !(binding.nombreText.text.isNullOrEmpty() || binding.apellidosText.text.isNullOrEmpty() || binding.userNameText.text.isNullOrEmpty()
                || binding.passwordText.text.isNullOrEmpty() || binding.mailText.text.isNullOrEmpty() || binding.phoneText.text.isNullOrEmpty() ||
                binding.confirmarPasswordText.text.isNullOrEmpty() || !binding.passwordText.text.toString()
            .equals(binding.confirmarPasswordText.text.toString()))
    }

    /**
     * Deshabilita la función de logout del menu overflow
     */
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem = menu.findItem(R.id.logout)
        item.isVisible = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0) {
            binding.registrar -> {

                val nombre = binding.nombreText.text.toString()
                val apellidos = binding.apellidosText.text.toString()
                val userName = binding.userNameText.text.toString()
                val password = binding.passwordText.text.toString()
                val email = binding.mailText.text.toString()
                val telefono = binding.phoneText.text.toString()

                val usuario = Usuario(
                    nombre,
                    apellidos,
                    userName,
                    Hash.hash(password),
                    email,
                    telefono
                )

                registraUsuario(usuario)
            }
            binding.cancelar -> {
                val navController = NavHostFragment.findNavController(this)
                navController.navigate(R.id.action_fragmentRegistrar_to_fragmentInicio2)
            }
        }
    }

    /**
     * Registra al usuario, y si el resultado es correcto, habilita el menú drawer de nuevo
     *
     * @param usuario usuario a registrar
     */
    fun registraUsuario(usuario: Usuario) {

        val contexto = this
        CoroutineScope(Dispatchers.Main).launch {

            val resultado = ApiRestAdapter.addUsuario(usuario).await()

            if (resultado.mensaje.equals("Registro insertado")) {
                Toast.makeText(
                    requireContext(),
                    "Registro correcto. Haz login para acceder a la aplicación",
                    Toast.LENGTH_LONG
                ).show()

                actividadPrincipal.enableDrawer()

                val navController = NavHostFragment.findNavController(contexto)
                navController.navigate(R.id.action_fragmentRegistrar_to_fragmentInicio2)

            } else if (resultado.mensaje.equals("Ya existe el usuario")) {
                Toast.makeText(
                    requireContext(),
                    resultado.mensaje,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    activity,
                    "Error al registrar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}