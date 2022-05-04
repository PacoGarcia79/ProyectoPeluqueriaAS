package com.pacogarcia.proyectopeluqueria.clasesestaticas

import android.content.Context
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Clase estática con métodos para el tratamiento de imágenes
 */
object ImagenUtilidad {

    /**
     * Método para redimensionar imágenes.
     */
    fun redimensionarImagenMaximo(mBitmap: Bitmap, newWidth: Float, newHeight: Float): Bitmap {
        val width = mBitmap.width
        val height = mBitmap.height
        val scaleWidth = newWidth / width
        val scaleHeight = newHeight / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false)
    }

    /**
     * Método para convertir imagen desde Base64.
     *
     * @param imagen Imagen en formato string.
     * @return Imagen en formato bitmap.
     */
    fun convertirStringBitmap(imagen: String?): Bitmap {
        val decodedString: ByteArray = Base64.decode(imagen, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    /**
     * Método para obtener bitmap desde un recurso de la aplicación
     *
     * @param recurso Id del recurso.
     * @param context Contexto.
     * @return Imagen en formato bitmap.
     */
    fun convertirRecursoBitmap(recurso: Int, context: Context): Bitmap {
        return BitmapFactory.decodeResource(context.getResources(), recurso)
    }

    /**
     * Método para convertir imagen a Base64.
     *
     * @param imagen Imagen en formato bitmap.
     * @return Imagen en formato string.
     */
    fun convertirImagenString(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val byte_arr: ByteArray = stream.toByteArray()
        return Base64.encodeToString(byte_arr, Base64.DEFAULT)
    }

    /**
     * Método para obtener el array de bytes de una imagen.
     *
     * @param imagen Imagen en formato bitmap.
     * @return Array de bytes.
     */
    fun getBytesFromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            return stream.toByteArray()
        }
        return null
    }

    /**
     * Método para obtener una imagen desde un array de bytes.
     *
     * @param imagenbyte Array de bytes.
     * @return Imagen en formato bitmap.
     */
    fun getFromBitmapBytes(imagenbyte: ByteArray?): Bitmap? {
        return if (imagenbyte != null) {
            BitmapFactory.decodeByteArray(imagenbyte, 0, imagenbyte.size)
        } else null
    }
}
