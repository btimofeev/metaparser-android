package org.emunix.metaparser.helper

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes


fun Context.showToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, msg, length).show()
}

fun Context.showToast(@StringRes resId: Int, length: Int = Toast.LENGTH_LONG) {
    val msg = this.getString(resId)
    Toast.makeText(this, msg, length).show()
}

fun View.visible(visible: Boolean) {
    if (visible)
        this.visibility = View.VISIBLE
    else
        this.visibility = View.GONE
}
