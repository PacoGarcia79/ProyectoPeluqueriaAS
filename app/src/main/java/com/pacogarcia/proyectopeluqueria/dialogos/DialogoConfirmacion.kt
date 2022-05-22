package com.pacogarcia.proyectopeluqueria.dialogos

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.pacogarcia.proyectopeluqueria.MainActivity

/**
 * Diálogo usado para la confirmación del usuario. Si hay click positivo, envía el resultado para ejecutar la opción para la que
 * se ha pedido confirmación.
 */
class DialogoConfirmacion : DialogFragment() {
    internal lateinit var listener: ConfirmacionDialogologListener

    interface ConfirmacionDialogologListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val args = requireArguments()
            val mensaje: String? = args.getString("mensaje")

            builder.setMessage(mensaje)
                .setPositiveButton("ACEPTAR",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogPositiveClick(this)

                        val result = true

                        if(MainActivity.dialogoAbiertoDesdeReservas){
                            setFragmentResult("reservasKey", bundleOf("bundleReservas" to result))
                            MainActivity.dialogoAbiertoDesdeReservas = false
                        }
                        if(MainActivity.dialogoAbiertoDesdeHorarios){
                            setFragmentResult("horariosKey", bundleOf("bundleHorarios" to result))
                            MainActivity.dialogoAbiertoDesdeHorarios = false
                        }
                    })
                .setNegativeButton("CANCELAR") { dialog, id ->
                        dialog.dismiss()
                    }

            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ConfirmacionDialogologListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }
}