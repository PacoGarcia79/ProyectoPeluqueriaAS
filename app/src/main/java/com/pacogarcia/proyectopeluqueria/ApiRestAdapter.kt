package com.pacogarcia.proyectopeluqueria

import com.pacogarcia.proyectopeluqueria.interfaces.ProveedorServicios
import com.pacogarcia.proyectopeluqueria.modelos.*
import kotlinx.coroutines.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

/**
 * Clase que define los métodos necesarios para usar el servicio de API Rest
 */
object ApiRestAdapter {

    var cookie: String? = null

    const val url = "http://ubuntubalmispacogarcia.eastus.cloudapp.azure.com:8081/apirest/"

    fun crearRetrofit(): ProveedorServicios {

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ProveedorServicios::class.java)
    }

    /**
     * Método que obtiene la cookie necesaria a partir de los headers "Set-Cookie".
     */
    private fun limpiarCookie(): String? {
        return cookie?.split(';')?.get(0)
    }

    // <editor-fold defaultstate="collapsed" desc=" Login/Logout ">

    /**
     * Este método se usa para la autentificación del usuario.
     * @param u Objeto de tipo Usuario con los datos necesarios para el login.
     * @return Un objeto de tipo MensajeLogin para evaluar el resultado de la petición
     */
    fun autorizaUsuario(
        u: Usuario
    ): Deferred<MensajeLogin> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeLogin>
            var resp = MensajeLogin()

            response =
                proveedorServicios.autorizar(u)

            cookie = response.headers().get("Set-Cookie")

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }

            resp
        }
    }

    /**
     * Este método se usa para el logout del usuario.
     * @return Un objeto de tipo MensajeLogout para evaluar el resultado de la petición
     */
    fun logout(): Deferred<MensajeLogout> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeLogout>
            var resp = MensajeLogout()

            response =
                proveedorServicios.logout(limpiarCookie()!!)

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }

            resp
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Usuarios ">

    /**
     * Este método se usa para obtener los datos del usuario a partir de su id.
     *
     * @param idUsuario Id del usuario.
     * @return Un objeto de tipo usuario.
     */
    fun getUserData(idUsuario: Int): Deferred<Usuario> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<Usuario>
            var resp = Usuario()

            response =
                proveedorServicios.getUsuario(limpiarCookie()!!, idUsuario)

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }

            resp
        }
    }

    /**
     * Este método se usa para modificar los datos de un usuario.
     *
     * @param usuario usuario a modificar.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    fun modificarUsuario(usuario: Usuario): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.modificarUsuario(limpiarCookie()!!, usuario)

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }
            resp
        }
    }

    /**
     * Este método se usa para modificar los datos de un usuario, incluyendo su contraseña.
     *
     * @param usuario usuario a modificar.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    fun modificarUsuarioPassword(usuario: Usuario): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.modificarUsuarioPassword(limpiarCookie()!!, usuario)

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }
            resp
        }
    }

    /**
     * Este método se usa para obtener la lista de clientes.
     *
     * @return Un ArrayList de usuarios de rol cliente.
     */
    fun cargarClientes(): Deferred<ArrayList<Usuario>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Usuario>>
            var datos = ArrayList<Usuario>()
            response = proveedorServicios.getClientes(limpiarCookie()!!)
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este método se usa para obtener la lista de empleados.
     *
     * @return Un ArrayList de usuarios de rol empleado.
     */
    fun cargarEmpleados(): Deferred<ArrayList<Usuario>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Usuario>>
            var datos = ArrayList<Usuario>()
            response = proveedorServicios.getEmpleados(limpiarCookie()!!)
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este método se usa para añadir un cliente.
     *
     * @param usuario cliente a añadir.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    fun addUsuario(usuario: Usuario): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.registroUsuario(usuario)

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }

            resp
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Productos ">

    /**
     * Este método se usa para obtener la lista de productos de una búsqueda.
     *
     * @param query Query de la búsqueda.
     * @return Un ArrayList de Productos.
     */
    fun cargarProductosSearch(query: String): Deferred<ArrayList<Producto>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Producto>>
            var datos = ArrayList<Producto>()

            response = proveedorServicios.getProductosSearch(limpiarCookie()!!, query)
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este método se usa para obtener la lista de grupos de productos.
     *
     * @return Un ArrayList de objetos ProductoGrupo.
     */
    fun cargarProductoGrupos(): Deferred<ArrayList<ProductoGrupo>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<ProductoGrupo>>
            var datos = ArrayList<ProductoGrupo>()

            response = proveedorServicios.getProductoGrupos(limpiarCookie()!!)
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este método se usa para obtener la lista de productos por tipo de grupo.
     *
     * @param grupo Nombre del tipo de producto.
     * @return Un ArrayList de Productos.
     */
    fun cargarProductoPorGrupo(grupo: String): Deferred<ArrayList<Producto>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Producto>>
            var datos = ArrayList<Producto>()

            response = proveedorServicios.getProductosPorGrupo(limpiarCookie()!!, grupo)
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Citas ">

    /**
     * Este método se usa para obtener la lista de citas de los empleados.
     *
     * @param fechaComienzo Fecha de comienzo del periodo a buscar.
     * @param fechaFin Fecha de fin del periodo a buscar.
     * @param idUsuario Id del usuario empleado.
     * @return Un ArrayList de Citas.
     */
    fun cargarCitasEmpleado(
        fechaComienzo: String,
        fechaFin: String,
        idUsuario: Int
    ): Deferred<ArrayList<Cita>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Cita>>
            var datos = ArrayList<Cita>()
            response = proveedorServicios.getCitasEmpleado(
                limpiarCookie()!!,
                fechaComienzo,
                fechaFin,
                idUsuario
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este método se usa para obtener la lista de citas de los clientes.
     *
     * @param fechaComienzo Fecha de comienzo del periodo a buscar.
     * @param fechaFin Fecha de fin del periodo a buscar.
     * @param idUsuario Id del usuario cliente.
     * @return Un ArrayList de Citas.
     */
    fun cargarCitasCliente(
        fechaComienzo: String,
        fechaFin: String,
        idUsuario: Int
    ): Deferred<ArrayList<Cita>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Cita>>
            var datos = ArrayList<Cita>()
            response = proveedorServicios.getCitasCliente(
                limpiarCookie()!!,
                fechaComienzo,
                fechaFin,
                idUsuario
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este método se usa para obtener la lista completa de citas.
     *
     * @param fechaComienzo Fecha de comienzo del periodo a buscar.
     * @param fechaFin Fecha de fin del periodo a buscar.
     * @return Un ArrayList de Citas.
     */
    fun cargarCitasTotales(fechaComienzo: String, fechaFin: String): Deferred<ArrayList<Cita>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Cita>>
            var datos = ArrayList<Cita>()
            response = proveedorServicios.getTotalCitas(limpiarCookie()!!, fechaComienzo, fechaFin)
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este método se usa para cancelar una cita.
     *
     * @param citas cadena con los ids de las citas separados por coma.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    fun cancelarCitas(citas: String): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.cancelarCitas(limpiarCookie()!!, citas)

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }
            resp
        }
    }

    /**
     * Este método se usa para añadir un producto a una cita.
     *
     * @param idCita id de la cita a la que se añadirá el producto.
     * @param idProducto id del producto a añadir.
     * @param cantidadProducto cantidad del producto a añadir.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    fun addProductoCita(
        idCita: Int,
        idProducto: Int,
        cantidadProducto: Int
    ): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.addProductoCita(
                limpiarCookie()!!,
                idCita,
                idProducto,
                cantidadProducto
            )

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }
            resp
        }
    }

    /**
     * Este método se usa para añadir una cita.
     *
     * @param hora id del horario seleccionado.
     * @param empleado id del empleado al que se le añade la cita.
     * @param fecha fecha de la cita.
     * @param cliente id del cliente al que se le añade la cita.
     * @param servicios string con los id de los servicios separados por coma.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    fun addCita(
        hora: Int,
        empleado: Int,
        fecha: String,
        cliente: Int,
        servicios: String
    ): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.postCita(
                limpiarCookie()!!,
                hora,
                empleado,
                fecha,
                cliente,
                servicios
            )

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }
            resp
        }
    }

    /**
     * Este metodo se usa para obtener el listado de horarios libres en una fecha determinada, para la funcionalidad de citas.
     *
     * @param fecha Día en concreto para el que se quiere obtener el listado.
     * @return Un ArrayList de objetos Horario.
     */
    fun cargarHorariosLibresDia(fecha: String): Deferred<ArrayList<Horario>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Horario>>
            var datos = ArrayList<Horario>()
            response = proveedorServicios.getHorariosLibresDia(limpiarCookie()!!, fecha)
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este metodo se usa para obtener el listado de horarios libres por empleado en una fecha, para la funcionalidad de citas.
     *
     * @param fecha Día en concreto para el que se quiere obtener el listado.
     * @param usuario Id del empleado para el que se quiere obtener el listado.
     * @return Un ArrayList de objetos Horario.
     */
    fun cargarHorariosLibresEmpleadosFecha(
        usuario: Int,
        fecha: String
    ): Deferred<ArrayList<Horario>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Horario>>
            var datos = ArrayList<Horario>()
            response = proveedorServicios.getHorariosLibresEmpleadosFecha(
                limpiarCookie()!!,
                usuario,
                fecha
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este metodo se usa para obtener el listado de empleados libres en una fecha y horario determinado.
     *
     * @param fecha Día en concreto para el que se quiere obtener el listado.
     * @param idHorario Id del horario para el que se quiere obtener el listado.
     * @return Un ArrayList de usuarios de rol 'empleado'.
     */
    fun cargarEmpleadosDisponiblesOpcionHora(
        idHorario: Int,
        fecha: String
    ): Deferred<ArrayList<Usuario>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Usuario>>
            var datos = ArrayList<Usuario>()
            response = proveedorServicios.getEmpleadosDisponiblesOpcionHora(
                limpiarCookie()!!,
                idHorario,
                fecha
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este metodo se usa para obtener el listado de empleados libres en una fecha determinada.
     *
     * @param fecha Día en concreto para el que se quiere obtener el listado.
     * @return Un ArrayList de usuarios de rol 'empleado'.
     */
    fun cargarEmpleadosDisponiblesOpcionProfesional(
        fecha: String
    ): Deferred<ArrayList<Usuario>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Usuario>>
            var datos = ArrayList<Usuario>()
            response = proveedorServicios.getEmpleadosDisponiblesOpcionProfesional(
                limpiarCookie()!!,
                fecha
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este metodo se usa para obtener el listado de servicios que tiene un empleado determinado.
     *
     * @param idEmpleado Id del empleado para el que se quiere obtener el listado.
     * @return Un ArrayList de servicios.
     */
    fun cargarServiciosPorEmpleado(
        idEmpleado: Int
    ): Deferred<ArrayList<Servicio>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Servicio>>
            var datos = ArrayList<Servicio>()
            response = proveedorServicios.getServiciosPorEmpleado(
                limpiarCookie()!!,
                idEmpleado
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este metodo se usa para obtener el listado de fechas totalmente ocupadas, sin horarios libres para citas. El periodo engloba sesenta días.
     *
     * @param fechaComienzo Fecha de comienzo del periodo.
     * @return Un ArrayList de DateTime.
     */
    fun cargarFechasOcupadas(
        fechaComienzo: String
    ): Deferred<ArrayList<Date>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Date>>
            var datos = ArrayList<Date>()
            response = proveedorServicios.getFechasOcupadas(
                limpiarCookie()!!,
                fechaComienzo
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Horarios ">

    /**
     * Este metodo se usa para obtener el listado de no disponibilidad.
     *
     * @return Un ArrayList de objetos Disponibilidad.
     */
    fun cargarHorariosDeshabilitados(): Deferred<ArrayList<Disponibilidad>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Disponibilidad>>
            var datos = ArrayList<Disponibilidad>()
            response = proveedorServicios.getHorariosDeshabilitados(
                limpiarCookie()!!
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este metodo se usa para obtener el listado de horarios.
     *
     * @return Un ArrayList de objetos Horario.
     */
    fun cargarHorarios(): Deferred<ArrayList<Horario>> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()

        return CoroutineScope(Dispatchers.IO).async {
            val response: Response<ArrayList<Horario>>
            var datos = ArrayList<Horario>()
            response = proveedorServicios.getHorarios(
                limpiarCookie()!!
            )
            if (response.isSuccessful) {
                val datosResponse = response.body()
                if (datosResponse != null) {
                    datos = datosResponse
                }
            }
            datos
        }
    }

    /**
     * Este metodo se usa para añadir el/los horario/s del/los empleado/s al listado de no disponibilidad, para una fecha o un periodo de fechas.
     *
     * @param fechaComienzo Fecha de comienzo del periodo.
     * @param fechaFin Fecha de fin del periodo.
     * @param empleados String con los id de cada empleado separados por coma.
     * @param horas String con los id de los horarios de cada empleado separados por coma.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición.
     */
    fun putAddDisponibilidad(
        fechaComienzo: String,
        fechaFin: String,
        empleados: String,
        horas: String
    ): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.putAddDisponibilidad(
                limpiarCookie()!!,
                fechaComienzo,
                fechaFin,
                empleados,
                horas
            )

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }
            resp
        }
    }

    /**
     * Este metodo se usa para eliminar el/los horario/s del/los empleado/s del listado de no disponibilidad, usando los ids de los registros..
     *
     * @param listaIds String con el listado de ids de los registros de no disponibilidad que se quieren eliminar.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición.
     */
    fun putDelDisponibilidadIds(
        listaIds: String
    ): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.putDelDisponibilidadIds(
                limpiarCookie()!!,
                listaIds
            )

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }
            resp
        }
    }

    /**
     * Este metodo se usa para eliminar el/los horario/s del/los empleado/s del listado de no disponibilidad, para una fecha o un periodo de fechas.
     *
     * @param fechaComienzo Fecha de comienzo del periodo.
     * @param fechaFin Fecha de fin del periodo.
     * @param empleados String con los id de cada empleado separados por coma.
     * @param horas String con los id de los horarios de cada empleado separados por coma.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición.
     */
    fun putDelDisponibilidad(
        fechaComienzo: String,
        fechaFin: String,
        empleados: String,
        horas: String
    ): Deferred<MensajeGeneral> {
        val proveedorServicios: ProveedorServicios = crearRetrofit()
        return CoroutineScope(Dispatchers.IO).async {

            val response: Response<MensajeGeneral>
            var resp = MensajeGeneral()
            response = proveedorServicios.putDelDisponibilidad(
                limpiarCookie()!!,
                fechaComienzo,
                fechaFin,
                empleados,
                horas
            )

            if (response.isSuccessful) {
                val msgResponse = response.body()
                if (msgResponse != null) {
                    resp = msgResponse
                }
            }
            resp
        }
    }

    // </editor-fold>
}