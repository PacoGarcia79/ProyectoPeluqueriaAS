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
import com.jakewharton.rxbinding2.widget.RxTextView
import com.pacogarcia.proyectopeluqueria.clasesestaticas.Hash
import com.pacogarcia.proyectopeluqueria.clasesestaticas.ImagenUtilidad
import com.pacogarcia.proyectopeluqueria.modelos.Usuario
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


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

        respondeEventoCambioTextoEmail()
        respondeEventoCambioTextoPasswordConfirmacion()
        respondeEventoCambioTextoNombre()
        respondeEventoCambioTextoApellidos()
        respondeEventoCambioTextoUserName()
        respondeEventoCambioTextoTelefono()
        respondeEventoCambioTextoPassword()

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

    fun tomarFoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultadoCamara.launch(cameraIntent)
    }

    fun tomarGaleria() {
        val cameraIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        resultadoGaleria.launch(cameraIntent)
    }

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
                    R.id.opBorrarFoto -> {
                        binding.fotoUsuario.setImageBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.avatar
                            )
                        )
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

                    modificaUsuarioPassword(usuario)
                }
            }
        }
    }

    private fun modificaUsuario(usuario: Usuario) {
        CoroutineScope(Dispatchers.Main).launch {
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
        }
    }

    private fun modificaUsuarioPassword(usuario: Usuario) {
        CoroutineScope(Dispatchers.Main).launch {
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
        }
    }

    fun comprobarDatos(): Boolean {
        return !(binding.nombreBackText.text.isNullOrEmpty() || binding.apellidosText.text.isNullOrEmpty() || binding.userNameText.text.isNullOrEmpty()
                || binding.mailText.text.isNullOrEmpty() || binding.phoneText.text.isNullOrEmpty())
    }

    fun comprobarDatosContrasenya(): Boolean {
        return !(binding.nombreBackText.text.isNullOrEmpty() || binding.apellidosText.text.isNullOrEmpty() || binding.userNameText.text.isNullOrEmpty()
                || binding.passwordText.text.isNullOrEmpty() || binding.mailText.text.isNullOrEmpty() || binding.phoneText.text.isNullOrEmpty() ||
                binding.confirmarPasswordText.text.isNullOrEmpty() || !binding.passwordText.text.toString()
            .equals(binding.confirmarPasswordText.text.toString()))
    }

    fun getUsuarioDespuesModificar(idUsuario: Int) {
        CoroutineScope(Dispatchers.Main).launch {
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
        }
    }

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

    private inline fun retryWhenError(crossinline onError: (ex: Throwable) -> Unit): ObservableTransformer<String, String> =
        ObservableTransformer { observable ->
            observable.retryWhen { errors ->
                errors.flatMap {
                    onError(it)
                    Observable.just("")
                }
            }
        }

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

}
