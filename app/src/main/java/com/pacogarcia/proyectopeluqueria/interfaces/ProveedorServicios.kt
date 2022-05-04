package com.pacogarcia.proyectopeluqueria.interfaces

import com.pacogarcia.proyectopeluqueria.modelos.*
import retrofit2.Response
import retrofit2.http.*
import java.util.*
import kotlin.collections.ArrayList


interface ProveedorServicios {

    // <editor-fold defaultstate="collapsed" desc=" Login/Logout ">

    /**
     * Este método se usa para la autentificación del usuario.
     * @param usuario Objeto de tipo Usuario con los datos necesarios para el login.
     * @return Un objeto de tipo MensajeLogin para evaluar el resultado de la petición
     */
    @POST("api/auth/login")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun autorizar(@Body usuario: Usuario): Response<MensajeLogin>

    /**
     * Este método se usa para el logout del usuario.
     * @param usuario Objeto de tipo Usuario con los datos necesarios para el logout.
     * @return Un objeto de tipo MensajeLogout para evaluar el resultado de la petición
     */
    @POST("api/auth/logout")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun logout(@Header("Cookie") cookie: String): Response<MensajeLogout>

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Productos ">

    /**
     * Este método se usa para obtener la lista de productos.
     *
     * @return Un ArrayList de Productos.
     */
    @GET("api/peluqueria/productos")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getProductos(@Header("Cookie") cookie: String): Response<ArrayList<Producto>>

    /**
     * Este método se usa para obtener la lista de productos de una búsqueda.
     *
     * @param query Query de la búsqueda.
     * @return Un ArrayList de Productos.
     */
    @GET("api/peluqueria/productos/busqueda/{query}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getProductosSearch(
        @Header("Cookie") cookie: String,
        @Path("query") query: String
    ): Response<ArrayList<Producto>>

    /**
     * Este método se usa para obtener la lista de grupos de productos.
     *
     * @return Un ArrayList de objetos ProductoGrupo.
     */
    @GET("api/peluqueria/productosgrupos")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getProductoGrupos(@Header("Cookie") cookie: String): Response<ArrayList<ProductoGrupo>>

    /**
     * Este método se usa para obtener la lista de productos por tipo de grupo.
     *
     * @param grupo Nombre del tipo de producto.
     * @return Un ArrayList de Productos.
     */
    @GET("api/peluqueria/productos/{grupo}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getProductosPorGrupo(
        @Header("Cookie") cookie: String,
        @Path("grupo") grupo: String
    ): Response<ArrayList<Producto>>

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Citas ">

    /**
     * Este método se usa para añadir un producto a una cita.
     *
     * @param idCita id de la cita a la que se añadirá el producto.
     * @param idProducto id del producto a añadir.
     * @param cantidadProducto cantidad del producto a añadir.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    @POST("api/peluqueria/cita/producto/{idCita}/{idProducto}/{cantidadProducto}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun addProductoCita(
        @Header("Cookie") cookie: String,
        @Path("idCita") idCita: Int,
        @Path("idProducto") idProducto: Int,
        @Path("cantidadProducto") cantidadProducto: Int
    ): Response<MensajeGeneral>

    /**
     * Este método se usa para obtener la lista de citas de los clientes.
     *
     * @param fechaComienzo Fecha de comienzo del periodo a buscar.
     * @param fechaFin Fecha de fin del periodo a buscar.
     * @param idUsuario Id del usuario cliente.
     * @return Un ArrayList de Citas.
     */
    @GET("api/peluqueria/citas/cliente/{fechaComienzo}/{fechaFin}/{idUsuario}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getCitasCliente(
        @Header("Cookie") cookie: String,
        @Path("fechaComienzo") fechaComienzo: String, @Path("fechaFin") fechaFin: String,
        @Path("idUsuario") idUsuario: Int
    ): Response<ArrayList<Cita>>

    /**
     * Este método se usa para obtener la lista de citas de los empleados.
     *
     * @param fechaComienzo Fecha de comienzo del periodo a buscar.
     * @param fechaFin Fecha de fin del periodo a buscar.
     * @param idUsuario Id del usuario empleado.
     * @return Un ArrayList de Citas.
     */
    @GET("api/peluqueria/citas/empleado/{fechaComienzo}/{fechaFin}/{idUsuario}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getCitasEmpleado(
        @Header("Cookie") cookie: String,
        @Path("fechaComienzo") fechaComienzo: String, @Path("fechaFin") fechaFin: String,
        @Path("idUsuario") idUsuario: Int
    ): Response<ArrayList<Cita>>

    /**
     * Este método se usa para obtener la lista completa de citas.
     *
     * @param fechaComienzo Fecha de comienzo del periodo a buscar.
     * @param fechaFin Fecha de fin del periodo a buscar.
     * @return Un ArrayList de Citas.
     */
    @GET("api/peluqueria/citas/{fechaComienzo}/{fechaFin}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getTotalCitas(
        @Header("Cookie") cookie: String,
        @Path("fechaComienzo") fechaComienzo: String,
        @Path("fechaFin") fechaFin: String
    ): Response<ArrayList<Cita>>

    /**
     * Este método se usa para cancelar una cita.
     *
     * @param citas cadena con los ids de las citas separados por coma.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    @PUT("api/peluqueria/citas/cancelar/{citas}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun cancelarCitas(
        @Header("Cookie") cookie: String,
        @Path("citas") citas: String
    ): Response<MensajeGeneral>

    /**
     * Este metodo se usa para obtener el listado de fechas totalmente ocupadas, sin horarios libres para citas. El periodo engloba sesenta días.
     *
     * @param fechaComienzo Fecha de comienzo del periodo.
     * @return Un ArrayList de DateTime.
     */
    @GET("api/peluqueria/fechas/ocupadas/{fechaComienzo}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getFechasOcupadas(
        @Header("Cookie") cookie: String,
        @Path("fechaComienzo") fechaComienzo: String
    ): Response<ArrayList<Date>>

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
    @POST("api/peluqueria/cita/{hora}/{empleado}/{fecha}/{cliente}/{servicios}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun postCita(
        @Header("Cookie") cookie: String,
        @Path("hora") hora: Int,
        @Path("empleado") empleado: Int,
        @Path("fecha") fecha: String,
        @Path("cliente") cliente: Int,
        @Path("servicios") servicios: String
    ): Response<MensajeGeneral>

    /**
     * Este metodo se usa para obtener el listado de horarios libres en una fecha determinada, para la funcionalidad de citas.
     *
     * @param fecha Día en concreto para el que se quiere obtener el listado.
     * @return Un ArrayList de objetos Horario.
     */
    @GET("api/peluqueria/horarios/libres/{fecha}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getHorariosLibresDia(
        @Header("Cookie") cookie: String,
        @Path("fecha") fecha: String
    ): Response<ArrayList<Horario>>

    /**
     * Este metodo se usa para obtener el listado de horarios libres por empleado en una fecha, para la funcionalidad de citas.
     *
     * @param fecha Día en concreto para el que se quiere obtener el listado.
     * @param usuario Id del empleado para el que se quiere obtener el listado.
     * @return Un ArrayList de objetos Horario.
     */
    @GET("api/peluqueria/horarios/libres/empleados/{usuario}/{fecha}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getHorariosLibresEmpleadosFecha(
        @Header("Cookie") cookie: String,
        @Path("usuario") usuario: Int,
        @Path("fecha") fecha: String
    ): Response<ArrayList<Horario>>

    /**
     * Este metodo se usa para obtener el listado de empleados libres en una fecha y horario determinado.
     *
     * @param fecha Día en concreto para el que se quiere obtener el listado.
     * @param idHorario Id del horario para el que se quiere obtener el listado.
     * @return Un ArrayList de usuarios de rol 'empleado'.
     */
    @GET("api/peluqueria/empleados/disponibles/{idHorario}/{fecha}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getEmpleadosDisponiblesOpcionHora(
        @Header("Cookie") cookie: String,
        @Path("idHorario") idHorario: Int,
        @Path("fecha") fecha: String
    ): Response<ArrayList<Usuario>>

    /**
     * Este metodo se usa para obtener el listado de empleados libres en una fecha determinada.
     *
     * @param fecha Día en concreto para el que se quiere obtener el listado.
     * @return Un ArrayList de usuarios de rol 'empleado'.
     */
    @GET("api/peluqueria/empleados/disponibles/fecha/{fecha}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getEmpleadosDisponiblesOpcionProfesional(
        @Header("Cookie") cookie: String,
        @Path("fecha") fecha: String
    ): Response<ArrayList<Usuario>>

    /**
     * Este metodo se usa para obtener el listado de servicios que tiene un empleado determinado.
     *
     * @param idEmpleado Id del empleado para el que se quiere obtener el listado.
     * @return Un ArrayList de servicios.
     */
    @GET("api/peluqueria/serviciosempleados/{idEmpleado}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getServiciosPorEmpleado(
        @Header("Cookie") cookie: String,
        @Path("idEmpleado") idEmpleado: Int
    ): Response<ArrayList<Servicio>>

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Usuarios ">

    /**
     * Este método se usa para obtener los datos del usuario a partir de su id.
     *
     * @param idUsuario Id del usuario.
     * @return Un objeto de tipo usuario.
     */
    @GET("api/peluqueria/usuarios/{idUsuario}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getUsuario(
        @Header("Cookie") cookie: String,
        @Path("idUsuario") idUsuario: Int
    ): Response<Usuario>

    /**
     * Este método se usa para modificar los datos de un usuario.
     *
     * @param usuario usuario a modificar.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    @PUT("api/peluqueria/usuarios/usuario")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun modificarUsuario(
        @Header("Cookie") cookie: String,
        @Body usuario: Usuario
    ): Response<MensajeGeneral>

    /**
     * Este método se usa para modificar los datos de un usuario, incluyendo su contraseña.
     *
     * @param usuario usuario a modificar.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    @PUT("api/peluqueria/usuarios/usuario/password")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun modificarUsuarioPassword(
        @Header("Cookie") cookie: String,
        @Body usuario: Usuario
    ): Response<MensajeGeneral>

    /**
     * Este método se usa para obtener la lista de clientes.
     *
     * @return Un ArrayList de usuarios de rol cliente.
     */
    @GET("api/peluqueria/clientes")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getClientes(@Header("Cookie") cookie: String): Response<ArrayList<Usuario>>

    /**
     * Este método se usa para añadir un usuario.
     *
     * @param usuario usuario a añadir.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición
     */
    @POST("api/peluqueria/registro")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun registroUsuario(@Body usuario: Usuario): Response<MensajeGeneral>

    /**
     * Este método se usa para obtener la lista de empleados.
     *
     * @return Un ArrayList de usuarios de rol empleado.
     */
    @GET("api/peluqueria/empleados")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getEmpleados(@Header("Cookie") cookie: String): Response<ArrayList<Usuario>>

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Horarios ">

    /**
     * Este metodo se usa para obtener el listado de no disponibilidad.
     *
     * @return Un ArrayList de objetos Disponibilidad.
     */
    @GET("api/peluqueria/horarios/listanodisponibilidad")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getHorariosDeshabilitados(@Header("Cookie") cookie: String): Response<ArrayList<Disponibilidad>>

    /**
     * Este metodo se usa para obtener el listado de horarios.
     *
     * @return Un ArrayList de objetos Horario.
     */
    @GET("api/peluqueria/horarios/")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun getHorarios(@Header("Cookie") cookie: String): Response<ArrayList<Horario>>

    /**
     * Este metodo se usa para añadir el/los horario/s del/los empleado/s al listado de no disponibilidad, para una fecha o un periodo de fechas.
     *
     * @param fechaComienzo Fecha de comienzo del periodo.
     * @param fechaFin Fecha de fin del periodo.
     * @param empleados String con los id de cada empleado separados por coma.
     * @param horas String con los id de los horarios de cada empleado separados por coma.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición.
     */
    @PUT("api/peluqueria/horarios/adddisponibilidad/{fechaComienzo}/{fechaFin}/{empleados}/{horas}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun putAddDisponibilidad(
        @Header("Cookie") cookie: String,
        @Path("fechaComienzo") fechaComienzo: String,
        @Path("fechaFin") fechaFin: String,
        @Path("empleados") empleados: String,
        @Path("horas") horas: String
    ): Response<MensajeGeneral>

    /**
     * Este metodo se usa para eliminar el/los horario/s del/los empleado/s del listado de no disponibilidad, usando los ids de los registros..
     *
     * @param listaIds String con el listado de ids de los registros de no disponibilidad que se quieren eliminar.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición.
     */
    @PUT("api/peluqueria/horarios/deldisponibilidad/{fechaComienzo}/{fechaFin}/{empleados}/{horas}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun putDelDisponibilidad(
        @Header("Cookie") cookie: String,
        @Path("fechaComienzo") fechaComienzo: String,
        @Path("fechaFin") fechaFin: String,
        @Path("empleados") empleados: String,
        @Path("horas") horas: String
    ): Response<MensajeGeneral>

    /**
     * Este metodo se usa para eliminar el/los horario/s del/los empleado/s del listado de no disponibilidad, para una fecha o un periodo de fechas.
     *
     * @param fechaComienzo Fecha de comienzo del periodo.
     * @param fechaFin Fecha de fin del periodo.
     * @param empleados String con los id de cada empleado separados por coma.
     * @param horas String con los id de los horarios de cada empleado separados por coma.
     * @return Un objeto de tipo MensajeGeneral para evaluar el resultado de la petición.
     */
    @PUT("api/peluqueria/horarios/deldisponibilidad/ids/{listaIds}")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun putDelDisponibilidadIds(
        @Header("Cookie") cookie: String,
        @Path("listaIds") listaIds: String
    ): Response<MensajeGeneral>

    // </editor-fold>
}
