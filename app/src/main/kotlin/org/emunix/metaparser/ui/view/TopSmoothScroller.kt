package org.emunix.metaparser.ui.view

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller

class TopSmoothScroller(context: Context): LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
    }
}