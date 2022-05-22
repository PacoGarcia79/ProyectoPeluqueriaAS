package com.pacogarcia.proyectopeluqueria

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import com.pacogarcia.proyectopeluqueria.clasesestaticas.Hash
import com.pacogarcia.proyectopeluqueria.clasesestaticas.ImagenUtilidad
import com.pacogarcia.proyectopeluqueria.databinding.FragmentRegistrarBinding
import com.pacogarcia.proyectopeluqueria.modelos.Usuario
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.NoSuchElementException

/**
 * Fragmento para el registro del usuario
 */
class FragmentRegistrar : Fragment(), View.OnClickListener {

    private lateinit var actividadPrincipal: MainActivity
    private lateinit var binding: FragmentRegistrarBinding

    private lateinit var registerPermisosCamera: ActivityResultLauncher<String>
    private lateinit var registerPermisosGaleria: ActivityResultLauncher<String>
    private lateinit var resultadoCamara: ActivityResultLauncher<Intent>
    private lateinit var resultadoGaleria: ActivityResultLauncher<Intent>

    var hayFoto = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentRegistrarBinding.inflate(inflater, container, false)

        actividadPrincipal = (requireActivity() as MainActivity)

        creaContratos()

        binding.fotoUsuario.setOnClickListener {
            showPopup(binding.fotoUsuario)
            true
        }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerPermisosCamera =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it == true) {
                    tomarFoto()
                }
            }
        registerPermisosGaleria =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it == true) {
                    tomarGaleria()
                }
            }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.menu_popup)
        popup.setOnMenuItemClickListener(
            PopupMenu.OnMenuItemClickListener
            { item: MenuItem? ->
                when (item!!.itemId) {
                    R.id.opHacerFoto -> {
                        registerPermisosCamera.launch(Manifest.permission.CAMERA)
                    }
                    R.id.opAbrirGaleria -> {
                        registerPermisosGaleria.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    R.id.opCancelar -> {
                        popup.dismiss()
                    }
                }
                true
            })
        popup.show()
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

                val usuario = Usuario()
                usuario.nombre = binding.nombreText.text.toString()
                usuario.apellidos = binding.apellidosText.text.toString()
                usuario.username = binding.userNameText.text.toString()
                usuario.password = Hash.hash(binding.passwordText.text.toString())
                usuario.email = binding.mailText.text.toString()
                usuario.telefono = binding.phoneText.text.toString()

                setFotoUsuario(usuario)

                registraUsuario(usuario)
            }
            binding.cancelar -> {
                val navController = NavHostFragment.findNavController(this)
                navController.navigate(R.id.action_fragmentRegistrar_to_fragmentInicio2)
            }
        }
    }

    private fun setFotoUsuario(usuario: Usuario) {
        val foto = binding.fotoUsuario.drawable?.let { it as BitmapDrawable }
        if (foto != null) {
            val fotoRed =
                ImagenUtilidad.redimensionarImagenMaximo(foto.bitmap!!, 200f, 200f)
            usuario.foto = ImagenUtilidad.convertirImagenString(fotoRed)
        } else {
            usuario.foto = ImagenUtilidad.convertirImagenString(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.avatar
                )
            )
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

            try {

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

            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Lanza el intent para tomar la foto
     */
    fun tomarFoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultadoCamara.launch(cameraIntent)
    }

    /**
     * Lanza el intent para seleccionar una foto de la galería
     */
    fun tomarGaleria() {
        val cameraIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        resultadoGaleria.launch(cameraIntent)
    }

    /**
     * Registra las actividades para esperar resultados de otras activitys o fragments.
     * Se debe registrar la actividad para posibles resultados en el método onCreate().
     */
    fun creaContratos() {

        resultadoCamara =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    hayFoto = true
                    binding.fotoUsuario.setImageBitmap(result.data?.extras?.get("data") as Bitmap)
                }
            }

        resultadoGaleria =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    hayFoto = true
                    binding.fotoUsuario.setImageURI(result.data?.data)
                }
            }
    }
}
