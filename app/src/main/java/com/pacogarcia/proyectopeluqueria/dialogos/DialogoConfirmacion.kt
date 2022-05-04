package com.pacogarcia.proyectopeluqueria.dialogos

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

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