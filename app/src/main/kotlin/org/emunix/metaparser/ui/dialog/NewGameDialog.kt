/*
 * Copyright (c) 2019 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.metaparser.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.emunix.metaparser.R


interface NewGameDialogListener {
    fun onDialogPositiveClick(dialog: DialogFragment)
    fun onDialogNegativeClick(dialog: DialogFragment)
}

class NewGameDialog : DialogFragment()  {

    private lateinit var listener: NewGameDialogListener

    companion object {
        fun newInstance(listener: NewGameDialogListener): NewGameDialog {
            val fragment = NewGameDialog()
            fragment.setListener(listener)
            return fragment
        }
    }

    fun setListener(listener: NewGameDialogListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!)
            .setTitle(R.string.dialog_new_game_title)
            .setMessage(R.string.dialog_new_game_text)
            .setPositiveButton(R.string.dialog_new_game_positive_button) { _, _ ->
                listener.onDialogPositiveClick(this)
            }
            .setNegativeButton(R.string.dialog_new_game_negative_button) { _, _ ->
                listener.onDialogNegativeClick(this)
            }
            .create()
    }
}