package se.umu.bio17dkg.sudokusolver

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class InvalidSudokuDialog: DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.invalid_sudoku_message)
                .setNeutralButton("Ok", DialogInterface.OnClickListener {
                    dialog, id ->

                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
}}