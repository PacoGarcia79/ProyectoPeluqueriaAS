package com.pacogarcia.proyectopeluqueria

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.pacogarcia.proyectopeluqueria.databinding.FragmentCuentaBinding
import com.pacogarcia.proyectopeluqueria.viewmodel.ItemViewModel
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.NavHostFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import com.pacogarcia.proyectopeluqueria.clasesestaticas.Hash
import com.pacogarcia.proyectopeluqueria.clasesestaticas.ImagenUtilidad
import com.pacogarcia.proyectopeluqueria.modelos.Usuario
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Fragmento para la gestión de los datos personales del usuario
 */
class FragmentCuenta : Fragment(), View.OnClickListener {

    private lateinit var registerPermisosCamera: ActivityResultLauncher<String>
    private lateinit var registerPermisosGaleria: ActivityResultLauncher<String>
    private lateinit var resultadoCamara: ActivityResultLauncher<Intent>
    private lateinit var resultadoGaleria: ActivityResultLauncher<Intent>

    private lateinit var binding: FragmentCuentaBinding
    private val model: ItemViewModel by activityViewModels()
    lateinit var botonAceptar: Button
    lateinit var front_anim: AnimatorSet
    lateinit var back_anim: AnimatorSet
    lateinit var front: ConstraintLayout
    lateinit var back: ConstraintLayout

    var isFront = true
    var hayFoto = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentCuentaBinding.inflate(inflater, container, false)

        creaContratos()

        binding.fotoUsuario.setOnClickListener() {
            showPopup(binding.fotoUsuario)
            true
        }

        setUserCard()

        botonAceptar = binding.aceptar
        botonAceptar.setOnClickListener(this)

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

        /**
         * Controla los cambios en los EditText para no dejar ningún campo vacío.
         * Además controla también el estado del botón aceptar según sea la comprobación de los datos y según sea el estado del switch
         */
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                if (!binding.switchPassword.isChecked) {
                    botonAceptar.isEnabled = comprobarDatos()
                } else {
                    botonAceptar.isEnabled = comprobarDatosContrasenya()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        }

        binding.confirmarPasswordText.addTextChangedListener(textWatcher)
        binding.mailText.addTextChangedListener(textWatcher)
        binding.nombreBackText.addTextChangedListener(textWatcher)
        binding.apellidosText.addTextChangedListener(textWatcher)
        binding.userNameText.addTextChangedListener(textWatcher)
        binding.phoneText.addTextChangedListener(textWatcher)
        binding.passwordText.addTextChangedListener(textWatcher)

        iniciaCardFlip()

        /**
         * Controla los cambios en el switch, y según esos cambios activa o desactiva los campos de la contraseña y activa o
         * desactiva el botón aceptar.
         */
        binding.switchPassword.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.textBlockPassword.isEnabled = true
                binding.textInputPassword.isEnabled = true
                binding.textBlockConfirmarPassword.isEnabled = true
                binding.textInputConfirmPassword.isEnabled = true
                botonAceptar.isEnabled = false
            } else {
                binding.textBlockPassword.isEnabled = false
                binding.textInputPassword.isEnabled = false
                binding.textBlockConfirmarPassword.isEnabled = false
                binding.textInputConfirmPassword.isEnabled = false
                botonAceptar.isEnabled = true
            }
        }

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

                    editaUsuario()

                }
            }

        resultadoGaleria =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    hayFoto = true
                    binding.fotoUsuario.setImageURI(result.data?.data)

                    editaUsuario()
                }
            }
    }

    /**
     * Establece los campos de la tarjeta con los datos del usuario
     */
    private fun setUserCard() {
        val usuario = model.getUsuario.value
        val nombreCompleto = "${usuario?.nombre} ${usuario?.apellidos}"
        binding.nombreCompletoFront.text = nombreCompleto
        binding.emailFront.text = usuario?.email
        binding.phoneFront.text = usuario?.telefono
        binding.fotoUsuario.setImageBitmap(ImagenUtilidad.convertirStringBitmap(usuario?.foto))

        binding.nombreBackText.setText(usuario?.nombre)
        binding.apellidosText.setText(usuario?.apellidos)
        binding.userNameText.setText(usuario?.username)
        binding.mailText.setText(usuario?.email)
        binding.phoneText.setText(usuario?.telefono)
    }

    /**
     * Inicia y controla el movimiento de la tarjeta
     */
    private fun iniciaCardFlip() {
        val scale = requireContext().resources.displayMetrics.density

        front = binding.cardFront
        back = binding.cardBack

        val flip = binding.flipBtn
        val previous = binding.previous

        front.cameraDistance = 8000 * scale
        back.cameraDistance = 8000 * scale

        front_anim = AnimatorInflater.loadAnimator(
            context,
            R.animator.front_animator
        ) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(
            context,
            R.animator.back_animator
        ) as AnimatorSet

        flip.setOnClickListener {
            if (isFront) {
                binding.cardBack.visibility = View.VISIBLE
                front_anim.setTarget(front)
                back_anim.setTarget(back)
                front_anim.start()
                back_anim.start()
                isFront = false

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        binding.cardFront.visibility = View.GONE
                    },
                    1400 // value in milliseconds
                )
            }
        }

        previous.setOnClickListener {
            if (!isFront) {
                binding.cardFront.visibility = View.VISIBLE
                front_anim.setTarget(back)
                back_anim.setTarget(front)
                back_anim.start()
                front_anim.start()
                isFront = true

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        binding.cardBack.visibility = View.GONE
                    },
                    1400 // value in milliseconds
                )
            }
        }
    }

    /**
     * Gestiona el menú popup que se muestra al hacer click en la foto
     */
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

    override fun onClick(p0: View?) {
        when (p0) {
            botonAceptar -> {
                editaUsuario()
            }
        }
    }

    /**
     * Toma la información de los campos de datos del usuario y los modifica,
     * incluyendo su foto
     */
    private fun editaUsuario() {
        val nombre = binding.nombreBackText.text.toString()
        val apellidos = binding.apellidosText.text.toString()
        val userName = binding.userNameText.text.toString()
        val email = binding.mailText.text.toString()
        val telefono = binding.phoneText.text.toString()
        val password = binding.passwordText.text.toString()

        if (!binding.switchPassword.isChecked) {

            val usuario = Usuario()
            usuario.idUsuario = model.getUsuario.value?.idUsuario!!
            usuario.nombre = nombre
            usuario.apellidos = apellidos
            usuario.username = userName
            usuario.email = email
            usuario.telefono = telefono

            setFotoUsuario(usuario)

            modificaUsuario(usuario)

        } else {

            val usuario = Usuario()
            usuario.idUsuario = model.getUsuario.value?.idUsuario!!
            usuario.nombre = nombre
            usuario.apellidos = apellidos
            usuario.username = userName
            usuario.email = email
            usuario.password = Hash.hash(password)
            usuario.telefono = telefono

            setFotoUsuario(usuario)

            modificaUsuarioPassword(usuario)
        }
    }

    /**
     * Establece la foto del usuario que luego se convertirá a Base64 y se subirá a la base de datos
     *
     * @param usuario usuario nuevo
     */
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
     * Modifica los datos del usuario, sin incluir la contraseña
     *
     * @param usuario usuario a modificar
     */
    private fun modificaUsuario(usuario: Usuario) {
        CoroutineScope(Dispatchers.Main).launch {

            try {

                val resultado = ApiRestAdapter.modificarUsuario(usuario).await()

                if (resultado.mensaje.equals("Registro actualizado")) {

                    Toast.makeText(
                        requireContext(),
                        "Actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    getUsuarioDespuesModificar(model.getUsuario.value?.idUsuario!!)
                } else {
                    Toast.makeText(
                        activity,
                        "No se ha podido actualizar",
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
     * Modifica los datos del usuario, incluyendo la contraseña
     *
     * @param usuario usuario a modificar
     */
    private fun modificaUsuarioPassword(usuario: Usuario) {
        CoroutineScope(Dispatchers.Main).launch {

            try {

                val resultado = ApiRestAdapter.modificarUsuarioPassword(usuario).await()

                if (resultado.mensaje.equals("Registro actualizado")) {

                    Toast.makeText(
                        requireContext(),
                        "Actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    getUsuarioDespuesModificar(model.getUsuario.value?.idUsuario!!)
                } else {
                    Toast.makeText(
                        activity,
                        "No se ha podido actualizar",
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
     * Comprueba los campos cuando no se modifica la contraseña. Se usará para gestionar que el botón aceptar esté o no habilitado.
     */
    fun comprobarDatos(): Boolean {
        return !(binding.nombreBackText.text.isNullOrEmpty() || binding.apellidosText.text.isNullOrEmpty() || binding.userNameText.text.isNullOrEmpty()
                || binding.mailText.text.isNullOrEmpty() || binding.phoneText.text.isNullOrEmpty())
    }

    /**
     * Comprueba los campos cuando sí se modifica la contraseña. Se usará para gestionar que el botón aceptar esté o no habilitado.
     */
    fun comprobarDatosContrasenya(): Boolean {
        return !(binding.nombreBackText.text.isNullOrEmpty() || binding.apellidosText.text.isNullOrEmpty() || binding.userNameText.text.isNullOrEmpty()
                || binding.passwordText.text.isNullOrEmpty() || binding.mailText.text.isNullOrEmpty() || binding.phoneText.text.isNullOrEmpty() ||
                binding.confirmarPasswordText.text.isNullOrEmpty() || !binding.passwordText.text.toString()
            .equals(binding.confirmarPasswordText.text.toString()))
    }

    /**
     * Obtiene los datos del usuario después de la modificación, y los setea en la tarjeta.
     * Además, coloca la tarjeta en su cara delantera
     *
     * @param idUsuario id del usuario del que se quieren obtener los datos
     */
    fun getUsuarioDespuesModificar(idUsuario: Int) {
        CoroutineScope(Dispatchers.Main).launch {

            try {

                val job = ApiRestAdapter.getUserData(idUsuario).await()
                model.setUsuario(job)

                setUserCard()

                if (!isFront) {
                    binding.cardFront.visibility = View.VISIBLE
                    front_anim.setTarget(back)
                    back_anim.setTarget(front)
                    back_anim.start()
                    front_anim.start()
                    isFront = true

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.cardBack.visibility = View.GONE
                        },
                        1400 // value in milliseconds
                    )
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
        RxTextView.afterTextChangeEvents(binding.nombreBackText)
            .skipInitialValue()
            .map {
                binding.textInputNombreBack.error = null
                it.view().text.toString()
            }
            .debounce(
                400,
                TimeUnit.MILLISECONDS
            ).observeOn(AndroidSchedulers.mainThread())
            .compose(validateFieldsLength)
            .compose(retryWhenError {
                binding.textInputNombreBack.error = it.message
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

    fun navegarInicio(){
        val contextoFragment = this
        MainActivity.autorizado = false
        val navController = NavHostFragment.findNavController(contextoFragment)
        navController.navigate(com.pacogarcia.proyectopeluqueria.R.id.action_global_fragmentInicio2)
    }

}
