package org.emunix.metaparser.helper

import android.app.Application
import android.content.Context
import android.view.accessibility.AccessibilityManager
import timber.log.Timber

object AccessibilityHelper {
    fun isTouchExplorationEnabled(context: Context): Boolean {
        val am = context.getSystemService(Application.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val isTouchEnabled = am.isTouchExplorationEnabled
        Timber.d("Touch exploration is ${if (isTouchEnabled) "enabled" else "disabled"}")
        return isTouchEnabled
    }
}