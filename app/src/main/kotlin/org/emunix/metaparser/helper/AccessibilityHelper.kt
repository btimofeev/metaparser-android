/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */


package org.emunix.metaparser.helper

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TtsSpan
import android.view.accessibility.AccessibilityManager
import androidx.core.text.toSpanned
import dagger.hilt.android.qualifiers.ApplicationContext
import org.emunix.metaparser.R
import timber.log.Timber
import javax.inject.Inject

class AccessibilityHelper @Inject constructor(@ApplicationContext val context: Context) {

    fun isTouchExplorationEnabled(): Boolean {
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

    fun getSpannedCommand(command: String): Spanned =
        if (command.isNotEmpty() && command[0] == '>') {
            if (command.length > 1) {
                if (command.subSequence(1, command.length).isNotBlank()) {
                    wrapForTts(
                        command, 0, 1,
                        context.getString(R.string.span_command_accessibility)
                    )
                } else {
                    wrapForTts(command, 0, 1, "")
                }
            } else {
                wrapForTts(command, 0, 1, "")
            }
        } else {
            command.toSpanned()
        }
}