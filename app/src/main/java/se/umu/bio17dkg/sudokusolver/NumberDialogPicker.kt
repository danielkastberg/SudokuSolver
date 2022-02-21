package se.umu.bio17dkg.sudokusolver

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.fragment.app.DialogFragment


private lateinit var listener: NumberPicker.OnValueChangeListener

class NumberDialogPicker: DialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the OnValueChangeListener so we can send events to the host
            listener = context as OnValueChangeListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement OnValueChangeListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val numberPicker: NumberPicker = NumberPicker(it)
            numberPicker.minValue = 1
            numberPicker.maxValue = 9
            // Use the Builder class for convenient dialog construction
            val builder = android.app.AlertDialog.Builder(it)
            builder.setTitle("Choose Value")

            builder.setMessage("Choose a number :")
                .setPositiveButton(R.string.dialog_yes,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onValueChange(numberPicker, numberPicker.value, numberPicker.value)


                    })
                .setNegativeButton("Remove",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onValueChange(numberPicker, numberPicker.value, 0)
                        // User cancelled the dialog
                    })

            builder.setView(numberPicker)

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")



    }

    fun setValueChangeListener(valueChangeListener: OnValueChangeListener) {
        listener = valueChangeListener
    }


}


