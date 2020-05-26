package org.emunix.metaparser.helper

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TtsSpan
import android.view.accessibility.AccessibilityManager
import androidx.core.text.toSpanned
import org.emunix.metaparser.R
import timber.log.Timber

object AccessibilityHelper {
    fun isTouchExplorationEnabled(context: Context): Boolean {
        val am = context.getSystemService(Application.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val isTouchEnabled = am.isTouchExplorationEnabled
        Timber.d("Touch exploration is ${if (isTouchEnabled) "enabled" else "disabled"}")
        return isTouchEnabled
    }

    fun wrapForTts(message: CharSequence, start: Int, length: Int, ttsMessage: String): Spannable {
        val spannable = SpannableString(message)
        spannable.setSpan(TtsSpan.TextBuilder(ttsMessage).build(), start, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return spannable
    }

    fun getSpannedCommand(context: Context, command: String): Spanned {
        val spannedCommand: Spanned
        if (command.isNotEmpty() && command[0] == '>') {
            if (command.length > 1) {
                val rem = command.subSequence(1, command.length)
                if (rem.isNotBlank()) {
                    spannedCommand = wrapForTts(command, 0, 1,
                        context.getString(R.string.span_command_accessibility))
                } else {
                    spannedCommand = wrapForTts(command, 0, 1, "")
                }
            } else {
                spannedCommand = wrapForTts(command, 0, 1, "")
            }
        } else {
            spannedCommand = command.toSpanned()
        }

        return spannedCommand
    }
}